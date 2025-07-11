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

/**
 * Resolve a {@link DiscussionsActorService} according to the requested actor type.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
public interface DiscussionsActorServiceResolver
{
    /**
     * Returns an actor service for the requested type.
     * In case the actor service cannot be found  for the given type, fallback on a default service.
     *
     * @param type the type
     * @return the actor service corresponding to the type, or default service
     */
    DiscussionsActorService get(String type);
}
