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
 * Tests for {@link DefaultDiscussionReferencesResolver}.
 *
 * @version $Id$
 * @since 2.0
 */
@ComponentTest
class DefaultDiscussionReferencesResolverTest
{
    @InjectMockComponents
    private DefaultDiscussionReferencesResolver resolver;

    @Test
    void resolver()
    {
        String reference1 = "something;blabla";
        String reference2 = "foo;applicationHint=42";
        String reference3 = "null";
        String reference4 = "fooapplicationHint=bar";

        DiscussionReference discussionReference = new DiscussionReference("", reference1);
        assertEquals(discussionReference, this.resolver.resolve(reference1, DiscussionReference.class));

        discussionReference = new DiscussionReference("42", "foo");
        assertEquals(discussionReference, this.resolver.resolve(reference2, DiscussionReference.class));

        discussionReference = new DiscussionReference("", "null");
        assertEquals(discussionReference, this.resolver.resolve(reference3, DiscussionReference.class));

        discussionReference = new DiscussionReference("", reference4);
        assertEquals(discussionReference, this.resolver.resolve(reference4, DiscussionReference.class));

        MessageReference messageReference = new MessageReference("", reference1);
        assertEquals(messageReference, this.resolver.resolve(reference1, MessageReference.class));

        messageReference = new MessageReference("42", "foo");
        assertEquals(messageReference, this.resolver.resolve(reference2, MessageReference.class));

        messageReference = new MessageReference("", "null");
        assertEquals(messageReference, this.resolver.resolve(reference3, MessageReference.class));

        messageReference = new MessageReference("", reference4);
        assertEquals(messageReference, this.resolver.resolve(reference4, MessageReference.class));

        DiscussionContextReference discussionContextReference = new DiscussionContextReference("", reference1);
        assertEquals(discussionContextReference, this.resolver.resolve(reference1, DiscussionContextReference.class));

        discussionContextReference = new DiscussionContextReference("42", "foo");
        assertEquals(discussionContextReference, this.resolver.resolve(reference2, DiscussionContextReference.class));

        discussionContextReference = new DiscussionContextReference("", "null");
        assertEquals(discussionContextReference, this.resolver.resolve(reference3, DiscussionContextReference.class));

        discussionContextReference = new DiscussionContextReference("", reference4);
        assertEquals(discussionContextReference, this.resolver.resolve(reference4, DiscussionContextReference.class));
    }
}
