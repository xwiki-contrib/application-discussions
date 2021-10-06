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
import org.xwiki.contrib.discussions.domain.references.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.stability.Unstable;

/**
 * This service provides the operation to manipulate discussion objects.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
@Role
public interface DiscussionService
{
    /**
     * Creates a discussion with a main document.
     *
     * @param applicationHint the hint of the application used to create the discussion
     * @param title the discussion title
     * @param description the discussion description
     * @param mainDocument the main document to view the discussion
     * @param configurationParameters parameters used for data storage configuration
     * @return the created discussion
     */
    Optional<Discussion> create(String applicationHint, String title, String description, String mainDocument,
        DiscussionStoreConfigurationParameters configurationParameters);

    /**
     * Search for a discussion linked to the provided list of discussion contexts. If it exists, it is directly
     * returned. If it does not, it is initialized with the provided title and description, and linked to the list of
     * discussion contexts.
     *
     * @param applicationHint the hint of the application used to create the discussion
     * @param title the title
     * @param description the description
     * @param discussionContexts the list of discussion contexts
     * @param configurationParameters parameters used for data storage configuration
     * @return the created or found discussion
     */
    Optional<Discussion> getOrCreate(String applicationHint, String title, String description,
        List<DiscussionContextReference> discussionContexts,
        DiscussionStoreConfigurationParameters configurationParameters);

    /**
     * Search and retrieve a discussion by its reference.
     *
     * @param reference the discussion reference
     * @return the discussion
     */
    Optional<Discussion> get(DiscussionReference reference);

    /**
     * @param reference a discussion reference
     * @return {@code true} if the current user has the right to view the discussion, {@code false} otherwise
     */
    boolean canRead(DiscussionReference reference);

    /**
     * @param reference a discussion reference
     * @return {@code true} if the current user has the rights to write in the discussion, {@code false} otherwise
     */
    boolean canWrite(DiscussionReference reference);

    /**
     * Find a list of discussions that are linked at least to the list of discussion context passed in parameter.
     *
     * @param discussionContextReferences a list of discussion context references
     * @return the list discussions attached to the list of discussion contexts
     */
    List<Discussion> findByDiscussionContexts(List<DiscussionContextReference> discussionContextReferences);

    /**
     * Count the number of discussions linked to a context with the given entity references values.
     *
     * @param type the type of the entity reference
     * @param references the reference values of the entity references
     * @return the count result
     */
    long countByEntityReferences(String type, List<String> references);

    /**
     * Find the list of discussions linked to discussion contexts with the given entity references.
     *
     * @param type the entity reference type
     * @param references the entity reference values
     * @param offset the offset
     * @param limit the limit
     * @return tge paginated list of discussions
     */
    List<Discussion> findByEntityReferences(String type, List<String> references, Integer offset, Integer limit);

    /**
     * Set the update date of the discussion to now.
     *
     * @param discussionReference the reference of the discussion to update
     */
    void touch(DiscussionReference discussionReference);

    /**
     * Return true if a discussion exists with the request discussion context entity.
     *
     * @param reference the reference of the discussion context entity
     * @return {@code true} if a discussion context is found, {@code false} otherwise
     */
    boolean findByDiscussionContext(DiscussionContextEntityReference reference);

    /**
     * @return {@code true} if the current user can create a discussion, {@code false} otherwise
     */
    boolean canCreateDiscussion();

    /**
     * @param reference the reference of the discussion
     * @return {@code true} if the current user is allowed to view the discussion, {@code false} otherwise
     */
    boolean canViewDiscussion(DiscussionReference reference);
}
