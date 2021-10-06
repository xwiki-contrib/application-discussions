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
import org.xwiki.contrib.discussions.store.internal.AbstractDiscussionStoreConfiguration;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.stability.Unstable;

/**
 * Represents the configuration of a discussion store that can be implemented for any application using the discussion.
 * This allows applications using discussion to chose where to store the various elements of discussion: the
 * configuration implementations must then use the same hint as given in the APIs.
 *
 * @see AbstractDiscussionStoreConfiguration
 * @version $Id$
 * @since 2.0
 */
@Unstable
@Role
public interface DiscussionStoreConfiguration
{
    /**
     * @return the space where to store the discussion contexts.
     */
    SpaceReference getDiscussionContextSpaceStorageLocation();

    /**
     * @return the space where to store the discussions.
     */
    SpaceReference getDiscussionSpaceStorageLocation();

    /**
     * @return the space where to store the messages.
     */
    SpaceReference getMessageSpaceStorageLocation();
}
