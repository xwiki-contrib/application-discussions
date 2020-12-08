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

import java.util.Optional;

import org.xwiki.component.annotation.Role;
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
public interface DiscussionContextStoreService
{
    /**
     * Creates a discussion context object.
     *
     * @param name the name of the discussion context
     * @param description the description of the discussion context
     * @param referenceType the type of the entity referenced by the discussion context
     * @param entityReference the reference of the entity referenced by the discussion context
     * @return the unique reference to the created discussion context
     */
    Optional<String> create(String name, String description, String referenceType,
        String entityReference);

    /**
     * Returns a discussion context by its reference.
     *
     * @param reference the discussion context reference
     * @return the discussion context base object
     */
    Optional<BaseObject> get(String reference);

    /**
     * Links (unidirectionally) a discussion context and a discussion.
     *
     * @param discussionContextReference the discussion context reference
     * @param discussionReference the discussion reference
     */
    void link(String discussionContextReference, String discussionReference);

    /**
     * Unlinks (unidirectionally) a discussion context and a discussion.
     *
     * @param discussionContextReference the discussion context reference
     * @param discussionReference the discussion reference
     */
    void unlink(String discussionContextReference, String discussionReference);

    /**
     * Search for a discussion context by its reference.
     *
     * @param referenceType the reference type
     * @param entityReference the entity reference
     * @return the discussion context
     */
    Optional<BaseObject> findByReference(String referenceType, String entityReference);
}
