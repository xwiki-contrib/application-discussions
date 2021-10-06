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
package org.xwiki.contrib.discussions.internal;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.domain.references.AbstractDiscussionReference;

/**
 * Default implementation of {@link DiscussionReferencesSerializer}.
 * This serialization is based on the format defined for XWiki page references with a value.
 * {@see <a href="https://extensions.xwiki.org/xwiki/bin/view/Extension/Model%20Module#HPageReferences">XWiki page references</a>}
 * So if the {@code applicationHint} value is "foo" and the {@code reference} is "bar" the serialization will be
 * {@code bar;applicationHint=foo}.
 *
 * @version $Id$
 * @since 2.0
 */
@Component
@Singleton
public class DefaultDiscussionReferencesSerializer implements DiscussionReferencesSerializer
{
    @Override
    public String serialize(AbstractDiscussionReference discussionReference)
    {
        String result = discussionReference.getReference();
        if (!StringUtils.isEmpty(discussionReference.getApplicationHint())) {
            result = String.format("%s;applicationHint=%s", result, discussionReference.getApplicationHint());
        }
        return result;
    }
}
