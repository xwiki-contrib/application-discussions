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
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.objects.BaseObject;

/**
 * Low-level storage service for the discussion context objects.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
@Role
public interface DiscussionStoreService
{
    /**
     * Creates a discussion object.
     *
     * @param applicationHint the hint of the application used to create the discussion
     * @param title the title
     * @param description the description
     * @param mainDocument the main document to view the discussion
     * @param configurationParameters parameters used for data storage configuration
     * @return the unique reference to the created discussion
     */
    Optional<DiscussionReference> create(String applicationHint, String title, String description, String mainDocument,
        DiscussionStoreConfigurationParameters configurationParameters);

    /**
     * Resolve a discussion by its reference.
     *
     * @param reference the discussion reference
     * @return the discussion attributes
     */
    Optional<BaseObject> get(DiscussionReference reference);

    /**
     * Find the list of discussions attached to the list of discussion context references.
     *
     * @param discussionContextReferences the list of discussion context reference
     * @return the list of discussions
     */
    List<BaseObject> findByDiscussionContexts(List<DiscussionContextReference> discussionContextReferences);

    /**
     * Find the list of discussions attached to discussion contexts of the given type.
     *
     * @param type the entity type
     * @param references the entity reference
     * @param offset the offset
     * @param limit the limit
     * @return the paginated list of results
     */
    List<BaseObject> findByEntityReferences(String type, List<String> references, Integer offset, Integer limit);

    /**
     * Links (unidirectionally) a discussion to a discussion context.
     *
     * @param discussionReference the discussion reference
     * @param discussionContextReference the discussion context reference
     * @return {@code true} if the link was not already existing, {@code false} otherwise
     */
    boolean link(DiscussionReference discussionReference, DiscussionContextReference discussionContextReference);

    /**
     * Unlinks (unidirectionally) a discussion to a discussion context.
     *
     * @param discussionReference the discussion reference
     * @param discussionContextReference the discussion context reference
     * @return {@code true} if the link was existing and has been removed, {@code false} otherwise
     */
    boolean unlink(DiscussionReference discussionReference, DiscussionContextReference discussionContextReference);

    /**
     * Count the number of discussions linked to discussion contexts of a given type.
     *
     * @param type the type
     * @param references the discussion references
     * @return the count
     */
    long countByEntityReferences(String type, List<String> references);

    /**
     * Update the update date of the discussion.
     *
     * @param discussionReference the reference of the discussion
     */
    void touch(DiscussionReference discussionReference);
}
