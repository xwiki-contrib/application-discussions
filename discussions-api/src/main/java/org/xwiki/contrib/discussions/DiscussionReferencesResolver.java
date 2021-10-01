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
import org.xwiki.contrib.discussions.domain.references.AbstractDiscussionReference;
import org.xwiki.stability.Unstable;

/**
 * Resolve a serialized reference to a given {@link AbstractDiscussionReference}.
 *
 * @version $Id$
 * @since 2.0
 */
@Unstable
@Role
public interface DiscussionReferencesResolver
{
    /**
     * Perform the resolution of a given serialized reference to the given {@link AbstractDiscussionReference} concrete
     * type. Note that the serialization might not contain any information about the actual type, so it might not be
     * possible to check that the type is correct for this reference.
     *
     * @param serializedReference a serialization of an {@link AbstractDiscussionReference}.
     * @param type the concrete type to obtain
     * @param <T> the actual given concrete type
     * @return an instance of the given type with the information of the reference.
     */
    <T extends AbstractDiscussionReference> T resolve(String serializedReference, Class<T> type);
}
