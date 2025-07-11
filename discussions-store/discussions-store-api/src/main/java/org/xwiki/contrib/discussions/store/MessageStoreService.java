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
package org.xwiki.contrib.discussions.store;

import java.util.List;
import java.util.Optional;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.discussions.DiscussionException;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.domain.references.ActorReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.objects.BaseObject;

/**
 * Low-level storage service for the message objects.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
public interface MessageStoreService
{
    /**
     * Creates a message object.
     *
     * @param content the message content
     * @param syntax the syntax of the content of the message
     * @param authorReference the author reference
     * @param discussionReference the discussion reference
     * @param title the title of the discussion of the message
     * @param configurationParameters parameters used for data storage configuration
     * @return the unique reference of the created message
     */
    BaseObject create(String content, Syntax syntax, ActorReference authorReference,
        DiscussionReference discussionReference, String title,
        DiscussionStoreConfigurationParameters configurationParameters) throws DiscussionException;

    /**
     * Creates a message object in response to a message.
     *
     * @param content the message content
     * @param syntax the syntax of the content of the message
     * @param authorReference the author reference
     * @param originalMessage the original message this message replies to
     * @param title the title of the discussion of the message
     * @param configurationParameters parameters used for data storage configuration
     * @return the unique reference of the created message
     */
    default BaseObject createReplyTo(String content, Syntax syntax, ActorReference authorReference,
        Message originalMessage, String title, DiscussionStoreConfigurationParameters configurationParameters)
        throws DiscussionException
    {
        return null;
    }

    /**
     * Returns a paginate list of messages of a discussion.
     *
     * @param discussionReference the discussion reference
     * @param offset the offset
     * @param limit the limit
     * @return the list of messages
     */
    List<BaseObject> getByDiscussion(DiscussionReference discussionReference, int offset, int limit);

    /**
     * Returns the number of messages of a discussion.
     *
     * @param discussionReference the discussion reference
     * @return the count of messages of a discussion
     */
    long countByDiscussion(DiscussionReference discussionReference);

    /**
     * Get a message by its unique reference.
     *
     * @param reference the message reference
     * @return the object of the message
     */
    Optional<BaseObject> getByReference(MessageReference reference);

    /**
     * Get a message by the entity reference of its message object.
     *
     * @param entityReference the message object entity reference
     * @return the message base object
     */
    Optional<BaseObject> getByEntityReference(EntityReference entityReference);

    /**
     * Delete a message.
     *
     * @param reference the message reference
     */
    void delete(MessageReference reference);
}
