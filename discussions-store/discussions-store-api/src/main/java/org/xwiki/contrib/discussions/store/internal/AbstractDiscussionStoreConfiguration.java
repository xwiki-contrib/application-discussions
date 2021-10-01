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
package org.xwiki.contrib.discussions.store.internal;

import org.xwiki.contrib.discussions.store.DiscussionStoreConfiguration;
import org.xwiki.model.reference.SpaceReference;

/**
 * Abstract implementation of {@link DiscussionStoreConfiguration} which relies on a unique root space location and
 * store the different elements in subspaces named {@code DiscussionContext}, {@code Discussion} and {@code Message}.
 *
 * @version $Id$
 * @since 2.0
 */
public abstract class AbstractDiscussionStoreConfiguration implements DiscussionStoreConfiguration
{
    @Override
    public SpaceReference getDiscussionContextSpaceStorageLocation()
    {
        return new SpaceReference("DiscussionContext", getRootSpaceStorageLocation());
    }

    @Override
    public SpaceReference getDiscussionSpaceStorageLocation()
    {
        return new SpaceReference("Discussion", getRootSpaceStorageLocation());
    }

    @Override
    public SpaceReference getMessageSpaceStorageLocation()
    {
        return new SpaceReference("Message", getRootSpaceStorageLocation());
    }

    /**
     * @return the root space where to store the information.
     */
    public abstract SpaceReference getRootSpaceStorageLocation();
}
