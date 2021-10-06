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
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.events.DiscussionContextEvent;
import org.xwiki.contrib.discussions.events.DiscussionEvent;
import org.xwiki.contrib.discussions.store.DiscussionContextStoreService;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.observation.ObservationManager;

import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.events.ActionType.CREATE;
import static org.xwiki.contrib.discussions.events.ActionType.UPDATE;
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
    private ObservationManager observationManager;

    @Inject
    private DiscussionContextStoreService discussionContextStoreService;

    @Inject
    private DiscussionStoreService discussionStoreService;

    @Inject
    private DiscussionsRightService discussionsRightService;

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    @Override
    public Optional<DiscussionContext> create(String applicationHint, String name, String description,
        DiscussionContextEntityReference entityReference,
        DiscussionStoreConfigurationParameters configurationParameters)
    {
        return this.discussionContextStoreService.create(applicationHint, name, description, entityReference,
                configurationParameters)
            .map(reference -> {
                DiscussionContext discussionContext =
                    new DiscussionContext(reference, name, description, entityReference);
                this.observationManager.notify(
                    new DiscussionContextEvent(CREATE), applicationHint, discussionContext);
                return discussionContext;
            });
    }

    @Override
    public List<DiscussionContext> findByDiscussionReference(DiscussionReference reference)
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
    public boolean canViewDiscussionContext(DiscussionContextReference reference)
    {
        return this.discussionContextStoreService.get(reference)
            .map(o -> this.discussionsRightService.canWriteDiscussionContext(o.getDocumentReference()))
            .orElse(false);
    }

    @Override
    public Optional<DiscussionContext> getOrCreate(String applicationHint, String name, String description,
        DiscussionContextEntityReference entityReference,
        DiscussionStoreConfigurationParameters configurationParameters)
    {
        Optional<BaseObject> baseObject =
            this.discussionContextStoreService.findByReference(entityReference);
        if (baseObject.isPresent()) {
            return baseObject.flatMap(this::mapBaseObject);
        } else {
            return this.create(applicationHint, name, description, entityReference, configurationParameters);
        }
    }

    @Override
    public void link(DiscussionContext discussionContext, Discussion discussion)
    {
        DiscussionContextReference discussionContextReference = discussionContext.getReference();
        DiscussionReference discussionReference = discussion.getReference();
        String applicationHint = discussionContextReference.getApplicationHint();
        if (this.discussionContextStoreService.link(discussionContextReference, discussionReference)) {
            this.observationManager.notify(new DiscussionContextEvent(UPDATE), applicationHint, discussionContext);
        }
        if (this.discussionStoreService.link(discussionReference, discussionContextReference)) {
            this.observationManager.notify(new DiscussionEvent(UPDATE), applicationHint, discussion);
        }
    }

    @Override
    public void unlink(DiscussionContext discussionContext, Discussion discussion)
    {
        DiscussionContextReference discussionContextReference = discussionContext.getReference();
        DiscussionReference discussionReference = discussion.getReference();
        String applicationHint = discussionContextReference.getApplicationHint();

        if (this.discussionContextStoreService.unlink(discussionContextReference, discussionReference)) {
            this.observationManager.notify(new DiscussionContextEvent(UPDATE), applicationHint, discussionContext);
        }
        if (this.discussionStoreService.unlink(discussionReference, discussionContextReference)) {
            this.observationManager.notify(new DiscussionEvent(UPDATE), applicationHint, discussion);
        }
    }

    @Override
    public Optional<DiscussionContext> get(DiscussionContextReference reference)
    {
        return this.discussionContextStoreService.get(reference)
            .flatMap(this::mapBaseObject);
    }

    private Optional<DiscussionContext> mapBaseObject(BaseObject baseObject)
    {
        DiscussionContextReference discussionContextReference =
            this.discussionReferencesResolver.resolve(baseObject.getStringValue(REFERENCE_NAME),
                DiscussionContextReference.class);
        return Optional
            .of(new DiscussionContext(
                discussionContextReference,
                baseObject.getStringValue(NAME_NAME),
                baseObject.getStringValue(DESCRIPTION_NAME),
                new DiscussionContextEntityReference(
                    baseObject.getStringValue(ENTITY_REFERENCE_TYPE_NAME),
                    baseObject.getStringValue(ENTITY_REFERENCE_NAME)
                )
            ));
    }
}
