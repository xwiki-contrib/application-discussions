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
package org.xwiki.contrib.discussions.internal.converters;

import javax.inject.Inject;

import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.domain.references.AbstractDiscussionReference;
import org.xwiki.properties.converter.AbstractConverter;

/**
 * Abstract converter of {@link AbstractDiscussionReference} using standard resolver and serializer to perform
 * conversion.
 *
 * @param <T> the actual concrete type to convert.
 * @version $Id$
 * @since 2.0
 */
public abstract class AbstractDiscussionReferenceConverter<T extends AbstractDiscussionReference>
    extends AbstractConverter<T>
{
    @Inject
    protected DiscussionReferencesResolver discussionReferencesResolver;

    @Inject
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    @Override
    protected String convertToString(T value)
    {
        return this.discussionReferencesSerializer.serialize(value);
    }
}
