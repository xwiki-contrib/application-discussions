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

import java.util.Optional;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
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
     * @param name the discussion context name
     * @param description the discussion context description
     * @param referenceType the type of the entity referenced by the discussion context
     * @param entityReference the reference of the entity referenced by the discussion context
     * @return the initialized discussion context
     */
    Optional<DiscussionContext> create(String name, String description, String referenceType,
        String entityReference);
}
