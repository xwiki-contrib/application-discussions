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

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.stability.Unstable;

/**
 * API to manipulate the discussions rights.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
@Unstable
public interface DiscussionsRightsStoreService
{
    /**
     * Set a right to a user on a discussion.
     *
     * @param discussionReference the discussion reference
     * @param user the user
     * @param rightName the name of the right to add
     */
    void setDiscussionRightToUser(DiscussionReference discussionReference, DocumentReference user, String rightName);
}
