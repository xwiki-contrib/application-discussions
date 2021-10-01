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
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.domain.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.stability.Unstable;

/**
 * This service provides the operation to manipulate discussion context objects.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
@Unstable
public interface DiscussionContextService
{
    /**
     * Creates a discussion context.
     *
     * @param applicationHint the hint of the application used to create the context
     * @param name the discussion context name
     * @param description the discussion context description
     * @param entityReference the reference of the entity referenced by the discussion context
     * @return the initialized discussion context
     */
    Optional<DiscussionContext> create(String applicationHint, String name, String description,
        DiscussionContextEntityReference entityReference);

    /**
     * If a discussion context already exist with the given reference type and entity reference, it is returned. If it
     * does not the discussion context is created with the name and description passed in parameter, and it then
     * returned.
     *
     * @param applicationHint the hint of the application used to create the context
     * @param name the name
     * @param description the description
     * @param entityReference the entity reference
     * @return the found or created discussion context
     */
    Optional<DiscussionContext> getOrCreate(String applicationHint, String name, String description,
        DiscussionContextEntityReference entityReference);

    /**
     * Link a discussion context and a discussion.
     *
     * @param discussionContext the discussion context
     * @param discussion the discussion
     */
    void link(DiscussionContext discussionContext, Discussion discussion);

    /**
     * Unlink a discussion context and a discussion.
     *
     * @param discussionContext the discussion context
     * @param discussion the discussion
     */
    void unlink(DiscussionContext discussionContext, Discussion discussion);

    /**
     * Search and retrieve a discussion context by its reference.
     *
     * @param reference the discussion context reference
     * @return the discussion context
     */
    Optional<DiscussionContext> get(DiscussionContextReference reference);

    /**
     * Returns the list of discussion contexts linked to the discussion reference.
     *
     * @param reference the discussion reference
     * @return the list of discussion contexts
     */
    List<DiscussionContext> findByDiscussionReference(DiscussionReference reference);

    /**
     * @return {@code true} if the current actor can create a discussion context, {@code false} otherwise
     */
    boolean canCreateDiscussionContext();

    /**
     * @param reference the reference of the discussion context
     * @return {@code true} if the current user can view the discussion context, {@code false} otherwise
     */
    boolean canViewDiscussionContext(DiscussionContextReference reference);
}
