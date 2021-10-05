/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.discussions.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.events.DiscussionEvent;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.observation.ObservationManager;

import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.events.ActionType.CREATE;
import static org.xwiki.contrib.discussions.events.ActionType.UPDATE;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.MAIN_DOCUMENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.TITLE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.UPDATE_DATE_NAME;

/**
 * Default implementation of {@link DefaultDiscussionService}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultDiscussionService implements DiscussionService
{
    @Inject
    private ObservationManager observationManager;

    @Inject
    private DiscussionStoreService discussionStoreService;

    @Inject
    private DiscussionsRightService discussionsRightService;

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    @Override
    public Optional<Discussion> create(String applicationHint, String title, String description, String mainDocument)
    {
        Optional<Discussion> discussion =
            this.discussionStoreService.create(applicationHint, title, description, mainDocument).flatMap(this::get);
        discussion.ifPresent(d -> this.observationManager.notify(new DiscussionEvent(CREATE), applicationHint, d));
        return discussion;
    }

    @Override
    public Optional<Discussion> getOrCreate(String applicationHint, String title, String description,
        List<DiscussionContextReference> discussionContexts)
    {
        List<BaseObject> byDiscussionContexts =
            this.discussionStoreService.findByDiscussionContexts(discussionContexts);
        if (byDiscussionContexts.isEmpty()) {
            Optional<DiscussionReference> discussionReferenceOpt =
                this.discussionStoreService.create(applicationHint, title, description, null);
            discussionReferenceOpt.ifPresent(discussionReference -> discussionContexts.forEach(
                discussionContextReference -> this.discussionStoreService
                    .link(discussionReference, discussionContextReference)));
            return discussionReferenceOpt.flatMap(this::get);
        }
        BaseObject baseObject = byDiscussionContexts.get(0);

        DiscussionReference discussionReference = this.discussionReferencesResolver
            .resolve(baseObject.getStringValue(REFERENCE_NAME), DiscussionReference.class);
        return this.get(discussionReference);
    }

    @Override
    public Optional<Discussion> get(DiscussionReference reference)
    {
        return this.discussionStoreService.get(reference).map(this::mapBaseObject);
    }

    @Override
    public boolean canRead(DiscussionReference reference)
    {
        return this.discussionStoreService.get(reference)
            .map(d -> this.discussionsRightService.canReadDiscussion(d.getDocumentReference())).orElse(false);
    }

    @Override
    public boolean canWrite(DiscussionReference reference)
    {
        return this.discussionStoreService.get(reference)
            .map(d -> this.discussionsRightService.canWriteDiscussion(d.getDocumentReference())).orElse(false);
    }

    @Override
    public List<Discussion> findByDiscussionContexts(List<DiscussionContextReference> discussionContextReferences)
    {
        return this.discussionStoreService
            .findByDiscussionContexts(discussionContextReferences)
            .stream()
            .map(this::mapBaseObject)
            .collect(Collectors.toList());
    }

    @Override
    public long countByEntityReferences(String type, List<String> references)
    {
        return this.discussionStoreService.countByEntityReferences(type, references);
    }

    @Override
    public List<Discussion> findByEntityReferences(String type, List<String> references, Integer offset, Integer limit)
    {
        return this.discussionStoreService.findByEntityReferences(type, references, offset, limit)
            .stream()
            .map(this::mapBaseObject)
            .collect(Collectors.toList());
    }

    @Override
    public void touch(DiscussionReference discussionReference)
    {
        this.discussionStoreService.touch(discussionReference);
        this.get(discussionReference).ifPresent(discussion -> this.observationManager
                .notify(new DiscussionEvent(UPDATE), discussionReference.getApplicationHint(), discussion));
    }

    @Override
    public boolean findByDiscussionContext(DiscussionContextEntityReference entityReference)
    {
        return countByEntityReferences(entityReference.getType(), Arrays.asList(entityReference.getReference())) > 0;
    }

    @Override
    public boolean canCreateDiscussion()
    {
        return this.discussionsRightService.canCreateDiscussion();
    }

    @Override
    public boolean canViewDiscussion(DiscussionReference reference)
    {
        return this.discussionStoreService.get(reference)
            .map(o -> this.discussionsRightService.canReadDiscussion(o.getDocumentReference()))
            .orElse(false);
    }

    private Discussion mapBaseObject(BaseObject baseObject)
    {
        DiscussionReference discussionReference = this.discussionReferencesResolver
            .resolve(baseObject.getStringValue(REFERENCE_NAME), DiscussionReference.class);
        return new Discussion(
            discussionReference,
            baseObject.getStringValue(TITLE_NAME),
            baseObject.getStringValue(DESCRIPTION_NAME),
            baseObject.getDateValue(UPDATE_DATE_NAME),
            baseObject.getStringValue(MAIN_DOCUMENT_NAME)
        );
    }
}
