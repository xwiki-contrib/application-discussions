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
package org.xwiki.contrib.discussions;

import java.util.List;
import java.util.Optional;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.domain.references.ActorReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * This service provides the operation to manipulate message objects.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
public interface MessageService
{
    /**
     * Creates a message for the current user.
     *
     * @param content the message content
     * @param syntax the syntax of the content of the message
     * @param discussionReference the discussion reference
     * @param configurationParameters parameters used for data storage configuration
     * @return the created message
     */
    Message create(String content, Syntax syntax, DiscussionReference discussionReference,
        DiscussionStoreConfigurationParameters configurationParameters) throws DiscussionException;

    /**
     * Creates a message for a specific user.
     *
     * @param content the message content
     * @param syntax the syntax of the content of the message
     * @param discussionReference the discussion reference
     * @param authorReference the author reference
     * @param configurationParameters parameters used for data storage configuration
     * @return the create message
     */
    Message create(String content, Syntax syntax, DiscussionReference discussionReference,
        ActorReference authorReference, DiscussionStoreConfigurationParameters configurationParameters)
        throws DiscussionException;

    /**
     * Creates a message for a specific user.
     *
     * @param content the message content
     * @param syntax the syntax of the content of the message
     * @param discussionReference the discussion reference
     * @param authorReference the author reference
     * @param notify {@code true} if the notifications for the message creation can be sent, {@code false}
     *     otherwise
     * @param configurationParameters parameters used for data storage configuration
     * @return the create message
     */
    Message create(String content, Syntax syntax, DiscussionReference discussionReference,
        ActorReference authorReference, boolean notify, DiscussionStoreConfigurationParameters configurationParameters)
        throws DiscussionException;

    /**
     * Creates a message in reply to an existing message.
     *
     * @param content the message content
     * @param syntax the syntax of the content of the message
     * @param originalMessage the existing message this message replies to
     * @param authorReference the author reference
     * @param notify {@code true} if the notifications for the message creation can be sent, {@code false}
     *     otherwise
     * @param configurationParameters parameters used for data storage configuration
     * @return the create message
     */
    default Message createReplyTo(String content, Syntax syntax, Message originalMessage,
        ActorReference authorReference, boolean notify, DiscussionStoreConfigurationParameters configurationParameters)
        throws DiscussionException
    {
        return null;
    }

    /**
     * Get a message by its unique reference.
     *
     * @param reference the reference
     * @return the message
     */
    Optional<Message> getByReference(MessageReference reference);

    /**
     * Returns the paginated list of messages of the discussion.
     *
     * @param discussionReference the discussion reference
     * @param offset the offset
     * @param limit the limit
     * @return the list of messages
     */
    List<Message> getByDiscussion(DiscussionReference discussionReference, int offset, int limit);

    /**
     * Returns the count of messages of a discussion.
     *
     * @param discussion the discussion
     * @return the count of messages
     */
    long countByDiscussion(Discussion discussion);

    /**
     * Checks if the message can be deleted by the current user.
     *
     * @param message the message
     * @return {@code true} of the current user can delete the message. {@code false} otherwise
     */
    boolean canDelete(Message message);

    /**
     * Delete a message.
     *
     * @param reference the message reference
     */
    void delete(MessageReference reference);

    /**
     * Safely renders the content of a message.
     *
     * @param messageReference the reference of the message to render
     * @return the result of the rendering in html
     */
    String renderContent(MessageReference messageReference);

    /**
     * Load a message object by its entity reference.
     *
     * @param entityReference the entity reference of the message object
     * @return the message object
     */
    Optional<Message> getByEntity(EntityReference entityReference);
}
