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
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.store.internal.AbstractDiscussionStoreConfiguration;
import org.xwiki.model.reference.SpaceReference;

/**
 * Represents the configuration of a discussion store that can be implemented for any application using the discussion.
 * This allows applications using discussion to chose where to store the various elements of discussion: the
 * configuration implementations must then use the same hint as given in the APIs.
 *
 * @see AbstractDiscussionStoreConfiguration
 * @version $Id$
 * @since 2.0
 */
@Role
public interface DiscussionStoreConfiguration
{
    /**
     * Retrieve a space reference where to store the discussion contexts information.
     *
     * @param parameters configuration parameters given in the API that might be used to compute the storage location
     * @param contextEntityReference entity the context is attached to that can be used to compute the storage location
     * @return the space where to store the discussion contexts
     */
    SpaceReference getDiscussionContextSpaceStorageLocation(DiscussionStoreConfigurationParameters parameters,
        DiscussionContextEntityReference contextEntityReference);

    /**
     * Retrieve a space reference where to store the discussion information.
     *
     * @param parameters configuration parameters given in the API that might be used to compute the storage location
     * @return the space where to store the discussions
     */
    SpaceReference getDiscussionSpaceStorageLocation(DiscussionStoreConfigurationParameters parameters);

    /**
     * Retrieve a space reference where to store the message information.
     *
     * @param parameters configuration parameters given in the API that might be used to compute the storage location
     * @param discussionReference reference of the discussion the message is attached to, that can be used to compute
     *                             the storage location
     * @return the space where to store the messages
     */
    SpaceReference getMessageSpaceStorageLocation(DiscussionStoreConfigurationParameters parameters,
        DiscussionReference discussionReference);
}
