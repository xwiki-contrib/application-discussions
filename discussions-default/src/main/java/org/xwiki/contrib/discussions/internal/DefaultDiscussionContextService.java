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
import org.xwiki.contrib.discussions.DiscussionContextService;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.domain.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.events.DiscussionContextEvent;
import org.xwiki.contrib.discussions.events.DiscussionEvent;
import org.xwiki.contrib.discussions.store.DiscussionContextStoreService;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.observation.ObservationManager;

import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.events.ActionType.CREATE;
import static org.xwiki.contrib.discussions.events.ActionType.UPDATE;
import static org.xwiki.contrib.discussions.events.DiscussionsEvent.EVENT_SOURCE;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.NAME_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.REFERENCE_NAME;

/**
 * Default implementation of {@link DiscussionContextService}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultDiscussionContextService implements DiscussionContextService
{
    @Inject
    private DiscussionContextStoreService discussionContextStoreService;

    @Inject
    private DiscussionStoreService discussionStoreService;

    @Inject
    private ObservationManager observationManager;

    @Inject
    private DiscussionsRightService discussionsRightService;

    @Override
    public Optional<DiscussionContext> create(String name, String description, String referenceType,
        String entityReference)
    {
        return this.discussionContextStoreService.create(name, description, referenceType, entityReference)
            .map(reference -> {
                DiscussionContext discussionContext = new DiscussionContext(reference, name, description,
                    new DiscussionContextEntityReference(referenceType, entityReference));
                this.observationManager.notify(new DiscussionContextEvent(CREATE), EVENT_SOURCE, discussionContext);
                return discussionContext;
            });
    }

    @Override
    public List<DiscussionContext> findByDiscussionReference(String reference)
    {
        return this.discussionContextStoreService.findByDiscussionReference(reference)
            .stream()
            .map(this::mapBaseObject)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    @Override
    public boolean canCreateDiscussionContext()
    {
        return this.discussionsRightService.canCreateDiscussionContext();
    }

    @Override
    public boolean canViewDiscussionContext(String reference)
    {
        return this.discussionContextStoreService.get(reference)
            .map(o -> this.discussionsRightService.canWriteDiscussionContext(o.getDocumentReference()))
            .orElse(false);
    }

    @Override
    public Optional<DiscussionContext> getOrCreate(String name, String description, String referenceType,
        String entityReference)
    {
        Optional<BaseObject> baseObject =
            this.discussionContextStoreService.findByReference(referenceType, entityReference);
        if (baseObject.isPresent()) {
            return baseObject.flatMap(this::mapBaseObject);
        } else {
            return this.create(name, description, referenceType, entityReference);
        }
    }

    @Override
    public void link(DiscussionContext discussionContext, Discussion discussion)
    {
        String discussionContextReference = discussionContext.getReference();
        String discussionReference = discussion.getReference();

        if (this.discussionContextStoreService.link(discussionContextReference, discussionReference)) {
            this.observationManager.notify(new DiscussionContextEvent(UPDATE), EVENT_SOURCE, discussionContext);
        }
        if (this.discussionStoreService.link(discussionReference, discussionContextReference)) {
            this.observationManager.notify(new DiscussionEvent(UPDATE), EVENT_SOURCE, discussion);
        }
    }

    @Override
    public void unlink(DiscussionContext discussionContext, Discussion discussion)
    {
        String discussionContextReference = discussionContext.getReference();
        String discussionReference = discussion.getReference();
        if (this.discussionContextStoreService.unlink(discussionContextReference, discussionReference)) {
            this.observationManager.notify(new DiscussionContextEvent(UPDATE), EVENT_SOURCE, discussionContext);
        }
        if (this.discussionStoreService.unlink(discussionReference, discussionContextReference)) {
            this.observationManager.notify(new DiscussionEvent(UPDATE), EVENT_SOURCE, discussion);
        }
    }

    @Override
    public Optional<DiscussionContext> get(String reference)
    {
        return this.discussionContextStoreService.get(reference)
            .flatMap(this::mapBaseObject);
    }

    private Optional<DiscussionContext> mapBaseObject(BaseObject baseObject)
    {
        return Optional
            .of(new DiscussionContext(
                baseObject.getStringValue(REFERENCE_NAME),
                baseObject.getStringValue(NAME_NAME),
                baseObject.getStringValue(DESCRIPTION_NAME),
                new DiscussionContextEntityReference(
                    baseObject.getStringValue(ENTITY_REFERENCE_TYPE_NAME),
                    baseObject.getStringValue(ENTITY_REFERENCE_NAME)
                )
            ));
    }
}
