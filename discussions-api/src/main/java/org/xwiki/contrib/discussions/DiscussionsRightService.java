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

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.security.authorization.RuleState;
import org.xwiki.stability.Unstable;

/**
 * Service to manage the rights of the discussions entities.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
@Unstable
public interface DiscussionsRightService
{
    /**
     * @return {@code true} if the current user can create a discussion, {@code false} otherwise
     */
    boolean canCreateDiscussion();

    /**
     * @return {@code true} if the current user can create a discussion context, {@code false} otherwise
     */
    boolean canCreateDiscussionContext();

    /**
     * @param discussion the discussion page
     * @return {@code true} of the current user can read the discussion, {@code false otherwise}
     */
    boolean canReadDiscussion(EntityReference discussion);

    /**
     * @param discussion the discussion page
     * @return {@code true} if the current user can write in the discussion, {@code false} otherwise.
     */
    boolean canWriteDiscussion(DocumentReference discussion);

    /**
     * @param discussionContext the discussion context page
     * @return {@code true} if the current user can write in the discussion context, {@code false} otherwise.
     */
    boolean canWriteDiscussionContext(DocumentReference discussionContext);

    /**
     * @param message the message
     * @param discussion the discussion page
     * @return {@code true} if the current user can delete the message, {@code false} otherwise
     */
    boolean canDeleteMessage(Message message, DocumentReference discussion);

    /**
     * @param discussion the discussion
     * @return {@code} true if the current user is an administrator of the discussion, {@code} false otherwise
     */
    boolean isAdminDiscussion(DocumentReference discussion);

    /**
     * Define the user right to read the discussion.
     *
     * @param discussion the discussion
     * @param user the user
     * @param state define if the right should be allowed or denied
     */
    void setRead(Discussion discussion, DocumentReference user, RuleState state);

    /**
     * Allow the user to write the discussion.
     *
     * @param discussion the discussion
     * @param user the user
     * @param state define if the right should be allowed or denied
     */
    void setWrite(Discussion discussion, DocumentReference user, RuleState state);
}
