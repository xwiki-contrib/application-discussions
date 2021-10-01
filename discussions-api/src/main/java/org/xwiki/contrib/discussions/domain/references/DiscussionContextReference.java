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
package org.xwiki.contrib.discussions.domain.references;

import org.xwiki.stability.Unstable;

/**
 * Represents the reference of a {@link org.xwiki.contrib.discussions.domain.DiscussionContext}.
 *
 * @see org.xwiki.contrib.discussions.domain.references.AbstractDiscussionReference
 * @version $Id$
 * @since 2.0
 */
@Unstable
public class DiscussionContextReference extends AbstractDiscussionReference
{
    /**
     * Default constructor.
     *
     * @param applicationHint the application in which the context is used.
     * @param reference the actual reference of the context.
     */
    public DiscussionContextReference(String applicationHint, String reference)
    {
        super(applicationHint, reference);
    }
}
