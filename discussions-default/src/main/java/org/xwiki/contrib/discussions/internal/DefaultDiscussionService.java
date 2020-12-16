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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.events.DiscussionEvent;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.observation.ObservationManager;

import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.events.ActionType.CREATE;
import static org.xwiki.contrib.discussions.events.ActionType.UPDATE;
import static org.xwiki.contrib.discussions.events.DiscussionsEvent.EVENT_SOURCE;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DESCRIPTION_NAME;
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
    private DiscussionStoreService discussionStoreService;

    @Inject
    private ObservationManager observationManager;

    @Inject
    private DiscussionsRightService discussionsRightService;

    @Override
    public Optional<Discussion> create(String title, String description)
    {
        Optional<Discussion> discussion = this.discussionStoreService.create(title, description).flatMap(this::get);
        discussion.ifPresent(d -> this.observationManager.notify(new DiscussionEvent(CREATE), EVENT_SOURCE, d));
        return discussion;
    }

    @Override
    public Optional<Discussion> getOrCreate(String title, String description, List<String> discussionContexts)
    {
        List<BaseObject> byDiscussionContexts =
            this.discussionStoreService.findByDiscussionContexts(discussionContexts);
        if (byDiscussionContexts.isEmpty()) {
            Optional<String> discussionReferenceOpt = this.discussionStoreService.create(title, description);
            discussionReferenceOpt.ifPresent(discussionReference -> discussionContexts.forEach(
                discussionContextReference -> this.discussionStoreService
                    .link(discussionReference, discussionContextReference)));
            return discussionReferenceOpt.flatMap(this::get);
        }
        return this.get(byDiscussionContexts.get(0).getStringValue(REFERENCE_NAME));
    }

    @Override
    public Optional<Discussion> get(String reference)
    {
        return this.discussionStoreService.get(reference).map(this::mapBaseObject);
    }

    @Override
    public boolean canRead(String reference)
    {
        return this.discussionStoreService.get(reference)
            .map(d -> this.discussionsRightService.canReadDiscussion(d.getDocumentReference())).orElse(false);
    }

    @Override
    public boolean canWrite(String reference)
    {
        return this.discussionStoreService.get(reference)
            .map(d -> this.discussionsRightService.canWriteDiscussion(d.getDocumentReference())).orElse(false);
    }

    @Override
    public List<Discussion> findByDiscussionContexts(List<String> discussionContextReferences)
    {
        return this.discussionStoreService
            .findByDiscussionContexts(discussionContextReferences)
            .stream()
            .map(this::mapBaseObject)
            .collect(Collectors.toList());
    }

    @Override
    public long countByEntityReference(String type, String reference)
    {
        return this.discussionStoreService.countByEntityReference(type, reference);
    }

    @Override
    public List<Discussion> findByEntityReference(String type, String reference, Integer offset, Integer limit)
    {
        return this.discussionStoreService.findByEntityReference(type, reference, offset, limit)
            .stream()
            .map(this::mapBaseObject)
            .collect(Collectors.toList());
    }

    @Override
    public void touch(String discussionReference)
    {
        this.discussionStoreService.touch(discussionReference);
        this.get(discussionReference).ifPresent(
            discussion -> this.observationManager.notify(new DiscussionEvent(UPDATE), EVENT_SOURCE, discussion));
    }

    @Override
    public boolean findByDiscussionContext(String type, String reference)
    {
        return countByEntityReference(type, reference) > 0;
    }

    @Override
    public boolean canCreateDiscussion()
    {
        return this.discussionsRightService.canCreateDiscussion();
    }

    @Override
    public boolean canViewDiscussion(String reference)
    {
        return this.discussionStoreService.get(reference)
            .map(o -> this.discussionsRightService.canReadDiscussion(o.getDocumentReference()))
            .orElse(false);
    }

    private Discussion mapBaseObject(BaseObject baseObject)
    {
        return new Discussion(
            baseObject.getStringValue(REFERENCE_NAME),
            baseObject.getStringValue(TITLE_NAME),
            baseObject.getStringValue(DESCRIPTION_NAME),
            baseObject.getDateValue(UPDATE_DATE_NAME)
        );
    }
}
