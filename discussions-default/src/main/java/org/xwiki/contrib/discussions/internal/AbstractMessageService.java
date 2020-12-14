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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.MessageService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.events.MessageEvent;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.MessageStoreService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.ObservationManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.events.ActionType.CREATE;
import static org.xwiki.contrib.discussions.events.ActionType.DELETE;
import static org.xwiki.contrib.discussions.events.DiscussionsEvent.EVENT_SOURCE;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CREATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.UPDATE_DATE_NAME;

/**
 * Common operation for the {@link MessageService}. Each implementation should provide it own security policy.
 *
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractMessageService implements MessageService
{
    private static final String DEFAULT_ACTOR_TYPE = "user";

    @Inject
    private MessageStoreService messageStoreService;

    @Inject
    private DiscussionService discussionService;

    @Inject
    private DiscussionStoreService discussionStoreService;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @Inject
    private ObservationManager observationManager;

    abstract DiscussionsRightService getDiscussionRightService();

    @Override
    public Optional<Message> create(String content, String discussionReference)
    {
        return canWriteDiscussion(discussionReference,
            () -> {
                DocumentReference author = this.xcontextProvider.get().getUserReference();
                String authorReference = this.entityReferenceSerializer.serialize(author);
                return this.messageStoreService
                    .create(content, DEFAULT_ACTOR_TYPE, authorReference, discussionReference)
                    .flatMap(reference -> {
                        this.discussionService.touch(discussionReference);
                        Optional<Message> messageOpt = getByReference(reference, discussionReference);
                        messageOpt
                            .ifPresent(m -> this.observationManager.notify(new MessageEvent(CREATE), EVENT_SOURCE, m));
                        return messageOpt;
                    });
            },
            Optional::empty
        );
    }

    @Override
    public Optional<Message> create(String content, String discussionReference, String authorType,
        String authorReference)
    {
        // TODO: factorize with the create method above 
        return canWriteDiscussion(discussionReference,
            () -> this.messageStoreService
                .create(content, authorType, authorReference, discussionReference)
                .flatMap(reference -> {
                    this.discussionService.touch(discussionReference);
                    Optional<Message> messageOpt = getByReference(reference, discussionReference);
                    messageOpt
                        .ifPresent(m -> this.observationManager.notify(new MessageEvent(CREATE), EVENT_SOURCE, m));
                    return messageOpt;
                }),
            Optional::empty
        );
    }

    @Override
    public Optional<Message> getByReference(String reference, String discussionReference)
    {
        return canReadDiscussion(discussionReference,
            () -> this.messageStoreService.getByReference(reference, discussionReference)
                .flatMap(messageObject -> this.discussionService.get(discussionReference)
                    .map(discussion -> convertToMessage(discussion).apply(messageObject))),
            Optional::empty
        );
    }

    @Override
    public List<Message> getByDiscussion(String discussionReference, int offset, int limit)
    {
        return this.discussionService.get(discussionReference)
            .map(discussion -> this.<List<Message>>canReadDiscussion(discussionReference,
                () -> this.messageStoreService.getByDiscussion(discussionReference, offset, limit)
                    .stream()
                    .map(convertToMessage(discussion))
                    .collect(Collectors.toList()),
                Collections::emptyList))
            .orElse(Collections.emptyList());
    }

    @Override
    public long countByDiscussion(Discussion discussion)
    {
        return this.messageStoreService.countByDiscussion(discussion.getReference());
    }

    @Override
    public boolean canDelete(Message message)
    {
        String discussionReference = message.getDiscussion().getReference();
        return this.discussionStoreService.get(discussionReference).map(
            baseObject -> getDiscussionRightService().canDeleteMessage(message, baseObject.getDocumentReference()))
            .orElse(false);
    }

    @Override
    public void delete(String reference, String discussionReference)
    {
        this.getByReference(reference, discussionReference)
            .ifPresent(message -> {
                if (this.canDelete(message)) {
                    this.messageStoreService.delete(message.getReference(), message.getDiscussion().getReference());
                    this.observationManager.notify(new MessageEvent(DELETE), EVENT_SOURCE, message);
                }
            });
    }

    private <T> T canWriteDiscussion(String reference, Supplier<T> allowed, Supplier<T> disallowed)
    {
        boolean canWriteDiscussion = this.discussionStoreService.get(reference)
            .map(z -> getDiscussionRightService().canWriteDiscussion(z.getDocumentReference()))
            .orElse(false);
        if (canWriteDiscussion) {
            return allowed.get();
        } else {
            return disallowed.get();
        }
    }

    private <T> T canReadDiscussion(String reference, Supplier<T> allowed, Supplier<T> disallowed)
    {
        boolean canReadDiscussion = this.discussionStoreService.get(reference)
            .map(baseObject -> getDiscussionRightService().canReadDiscussion(baseObject.getDocumentReference()))
            .orElse(false);
        if (canReadDiscussion) {
            return allowed.get();
        } else {
            return disallowed.get();
        }
    }

    private Function<BaseObject, Message> convertToMessage(Discussion discussion)
    {
        return bo -> new Message(
            bo.getStringValue(REFERENCE_NAME),
            bo.getLargeStringValue(CONTENT_NAME),
            bo.getStringValue(AUTHOR_TYPE_NAME),
            bo.getStringValue(AUTHOR_REFERENCE_NAME),
            bo.getDateValue(CREATE_DATE_NAME),
            bo.getDateValue(UPDATE_DATE_NAME),
            discussion
        );
    }
}
