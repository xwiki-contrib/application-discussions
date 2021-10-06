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

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link DefaultDiscussionReferencesSerializer}.
 *
 * @version $Id$
 * @since 2.0
 */
@ComponentTest
class DefaultDiscussionReferencesSerializerTest
{
    @InjectMockComponents
    private DefaultDiscussionReferencesSerializer serializer;

    @Test
    void serialize()
    {
        DiscussionContextReference discussionContextReference = new DiscussionContextReference("", "someref");
        assertEquals("someref", this.serializer.serialize(discussionContextReference));

        discussionContextReference = new DiscussionContextReference("somehint", "someref");
        assertEquals("someref;applicationHint=somehint", this.serializer.serialize(discussionContextReference));

        DiscussionReference discussionReference = new DiscussionReference("foo", "bar");
        assertEquals("bar;applicationHint=foo", this.serializer.serialize(discussionReference));

        discussionReference = new DiscussionReference("", "foo");
        assertEquals("foo", this.serializer.serialize(discussionReference));

        MessageReference messageReference = new MessageReference(null, "42");
        assertEquals("42", this.serializer.serialize(messageReference));

        messageReference = new MessageReference("null", "42");
        assertEquals("42;applicationHint=null", this.serializer.serialize(messageReference));
    }
}
