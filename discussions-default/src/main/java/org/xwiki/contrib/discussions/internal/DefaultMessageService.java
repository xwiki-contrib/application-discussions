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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.MessageService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.domain.MessageContent;
import org.xwiki.contrib.discussions.domain.references.ActorReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.contrib.discussions.events.MessageEvent;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.MessageStoreService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.ObservationManager;
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.events.ActionType.CREATE;
import static org.xwiki.contrib.discussions.events.ActionType.DELETE;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CREATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.DISCUSSION_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REPLY_TO_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.UPDATE_DATE_NAME;

/**
 * Default implementation of {@link MessageService}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultMessageService implements MessageService
{
    private static final String DEFAULT_ACTOR_TYPE = "user";

    @Inject
    private DiscussionsRightService discussionsRightService;

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

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    @Override
    public Optional<Message> create(String content, Syntax syntax, DiscussionReference discussionReference,
        DiscussionStoreConfigurationParameters configurationParameters)
    {
        DocumentReference author = this.xcontextProvider.get().getUserReference();
        String authorReference = this.entityReferenceSerializer.serialize(author);
        return this.create(content, syntax, discussionReference,
            new ActorReference(DEFAULT_ACTOR_TYPE, authorReference), configurationParameters);
    }

    @Override
    public Optional<Message> create(String content, Syntax syntax,
        DiscussionReference discussionReference, ActorReference authorReference,
        DiscussionStoreConfigurationParameters configurationParameters)
    {
        return create(content, syntax, discussionReference, authorReference, true, configurationParameters);
    }

    @Override
    public Optional<Message> create(String content, Syntax syntax,
        DiscussionReference discussionReference, ActorReference authorReference, boolean notify,
        DiscussionStoreConfigurationParameters configurationParameters)
    {
        String title = this.discussionService.get(discussionReference).map(Discussion::getTitle).orElse("");
        return this.messageStoreService
            .create(content, syntax, authorReference, discussionReference, title, configurationParameters)
            .flatMap(reference -> {
                this.discussionService.touch(discussionReference);
                Optional<Message> messageOpt = getByReference(reference);
                if (notify) {
                    messageOpt
                        .ifPresent(m -> this.observationManager
                            .notify(new MessageEvent(CREATE), discussionReference.getApplicationHint(), m));
                }
                return messageOpt;
            });
    }

    @Override
    public Optional<Message> createReplyTo(String content, Syntax syntax, Message originalMessage,
        ActorReference authorReference, boolean notify, DiscussionStoreConfigurationParameters configurationParameters)
    {
        DiscussionReference discussionReference = originalMessage.getDiscussion().getReference();
        String title = this.discussionService.get(discussionReference).map(Discussion::getTitle).orElse("");
        return this.messageStoreService
            .createReplyTo(content, syntax, authorReference, originalMessage, title, configurationParameters)
            .flatMap(reference -> {
                this.discussionService.touch(discussionReference);
                Optional<Message> messageOpt = getByReference(reference);
                if (notify) {
                    messageOpt
                        .ifPresent(m -> this.observationManager
                            .notify(new MessageEvent(CREATE), discussionReference.getApplicationHint(), m));
                }
                return messageOpt;
            });
    }

    @Override
    public Optional<Message> getByReference(MessageReference reference)
    {
        return this.messageStoreService.getByReference(reference)
            .flatMap(messageObject -> {
                String msgDiscussionReference = messageObject.getStringValue(DISCUSSION_REFERENCE_NAME);
                DiscussionReference discussionReference =
                    this.discussionReferencesResolver.resolve(msgDiscussionReference, DiscussionReference.class);
                return this.discussionService.get(discussionReference)
                    .map(discussion -> convertToMessage(discussion).apply(messageObject));
            });
    }

    @Override
    public List<Message> getByDiscussion(DiscussionReference discussionReference, int offset, int limit)
    {
        return this.discussionService.get(discussionReference)
            .map(discussion -> this.messageStoreService.getByDiscussion(discussionReference, offset, limit)
                .stream()
                .map(convertToMessage(discussion))
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    @Override
    public long countByDiscussion(Discussion discussion)
    {
        return this.messageStoreService.countByDiscussion(discussion.getReference());
    }

    @Override
    public void delete(MessageReference reference)
    {
        this.getByReference(reference)
            .ifPresent(message -> {
                this.messageStoreService.delete(message.getReference());
                this.observationManager.notify(new MessageEvent(DELETE), reference.getApplicationHint(), message);
            });
    }

    @Override
    public String renderContent(MessageReference messageReference)
    {
        return this.messageStoreService.getByReference(messageReference)
            .map(it -> it.getOwnerDocument().display(CONTENT_NAME, it, this.xcontextProvider.get()))
            .orElse("");
    }

    @Override
    public boolean canDelete(Message message)
    {
        DiscussionReference discussionReference = message.getDiscussion().getReference();
        return this.discussionStoreService.get(discussionReference).map(
            baseObject -> this.discussionsRightService.canDeleteMessage(message, baseObject.getDocumentReference()))
            .orElse(false);
    }

    @Override
    public Optional<Message> getByEntity(EntityReference entityReference)
    {
        return this.messageStoreService.getByEntityReference(entityReference)
            .flatMap(bo -> {
                DiscussionReference discussionReference = this.discussionReferencesResolver
                    .resolve(bo.getStringValue(DISCUSSION_REFERENCE_NAME), DiscussionReference.class);
                return this.discussionService.get(discussionReference)
                    .map(it -> convertToMessage(it).apply(bo));
            });
    }

    private Function<BaseObject, Message> convertToMessage(Discussion discussion)
    {
        return bo -> {
            MessageReference messageReference =
                this.discussionReferencesResolver.resolve(bo.getStringValue(REFERENCE_NAME), MessageReference.class);
            MessageReference replyToReference = null;
            if (!StringUtils.isEmpty(bo.getStringValue(REPLY_TO_NAME))) {
                replyToReference = this.discussionReferencesResolver
                    .resolve(bo.getStringValue(REPLY_TO_NAME), MessageReference.class);
            }
            return new Message(
                messageReference,
                new MessageContent(bo.getLargeStringValue(CONTENT_NAME), bo.getOwnerDocument().getSyntax()),
                new ActorReference(bo.getStringValue(AUTHOR_TYPE_NAME), bo.getStringValue(AUTHOR_REFERENCE_NAME)),
                bo.getDateValue(CREATE_DATE_NAME),
                bo.getDateValue(UPDATE_DATE_NAME),
                discussion,
                replyToReference
            );
        };
    }
}
