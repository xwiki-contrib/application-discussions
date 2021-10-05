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
package org.xwiki.contrib.discussions.messagestream.script;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.DiscussionContextService;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.domain.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.messagestream.internal.DiscussionsFollowersService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.user.group.GroupManager;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.discussions.messagestream.internal.DiscussionMessageStreamConfiguration.DISCUSSION_MESSAGESTREAM_HINT;

/**
 * Test of {@link DiscussionsMessageStreamScriptService}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class DiscussionsMessageStreamScriptServiceTest
{
    @InjectMockComponents
    private DiscussionsMessageStreamScriptService target;

    @MockComponent
    private DiscussionContextService discussionContextService;

    @MockComponent
    private GroupManager groupManager;

    @MockComponent
    private DocumentReferenceResolver<String> resolver;

    @MockComponent
    private EntityReferenceSerializer<String> serializer;

    @MockComponent
    private DiscussionsFollowersService discussionsFollowersService;

    @Test
    void initializeContextPublic()
    {
        DiscussionContext dc1 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc1"), "", "",
            new DiscussionContextEntityReference("messagestream-emitter", "Author"));
        DiscussionContext dc2 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc2"), "", "",
            new DiscussionContextEntityReference("messagestream-user", "Author"));
        DiscussionContext dc3 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc3"), "", "",
            new DiscussionContextEntityReference("messagestream-user", ""));

        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-emitter", "Author")))
            .thenReturn(Optional.of(dc1));
        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-user", "Author")))
            .thenReturn(Optional.of(dc2));
        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-user", "*")))
            .thenReturn(Optional.of(dc3));

        List<DiscussionContext> discussionContexts = this.target.initializeContextPublic("Author");
        assertEquals(3, discussionContexts.size());
        assertEquals(dc1, discussionContexts.get(0));
        assertEquals(dc2, discussionContexts.get(1));
        assertEquals(dc3, discussionContexts.get(2));
    }

    @Test
    void initializeContextFollowers()
    {
        DocumentReference authorDR = new DocumentReference("xwiki", "XWiki", "Author");
        DiscussionContext dc1 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc1"), "", "",
            new DiscussionContextEntityReference("messagestream-emitter", "Author"));
        DiscussionContext dc2 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc2"), "", "",
            new DiscussionContextEntityReference("messagestream-user", "Author"));
        DiscussionContext dc3 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc3"), "", "",
            new DiscussionContextEntityReference("messagestream-user", "xwiki:XWiki.Follower"));

        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-emitter", "Author")))
            .thenReturn(Optional.of(dc1));
        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-user", "Author")))
            .thenReturn(Optional.of(dc2));
        when(this.resolver.resolve("Author")).thenReturn(authorDR);
        when(this.serializer.serialize(authorDR)).thenReturn("xwiki:XWiki.Author");
        when(this.discussionsFollowersService.getFollowers("xwiki:XWiki.Author")).thenReturn(Arrays.asList(
            "xwiki:XWiki.Follower"
        ));
        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-user", "xwiki:XWiki.Follower")))
            .thenReturn(Optional.of(dc3));

        List<DiscussionContext> discussionContexts = this.target.initializeContextFollowers("Author");
        assertEquals(3, discussionContexts.size());
        assertEquals(dc1, discussionContexts.get(0));
        assertEquals(dc2, discussionContexts.get(1));
        assertEquals(dc3, discussionContexts.get(2));
    }

    @Test
    void initializeContextUsers()
    {
        DiscussionContext dc1 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc1"), "", "",
            new DiscussionContextEntityReference("messagestream-emitter", "Author"));
        DiscussionContext dc2 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc2"), "", "",
            new DiscussionContextEntityReference("messagestream-user", "Author"));
        DiscussionContext dc3 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc3"), "", "",
            new DiscussionContextEntityReference("messagestream-user", "xwiki:XWiki.user1"));
        DiscussionContext dc4 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc4"), "", "",
            new DiscussionContextEntityReference("messagestream-user", "xwiki:XWiki.user2"));
        DocumentReference user1Reference = new DocumentReference("xwiki", "XWiki", "user1");
        DocumentReference user2Reference = new DocumentReference("xwiki", "XWiki", "user2");

        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-emitter", "Author")))
            .thenReturn(Optional.of(dc1));
        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-user", "Author")))
            .thenReturn(Optional.of(dc2));
        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-user", "xwiki:XWiki.user1")))
            .thenReturn(Optional.of(dc3));
        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-user", "xwiki:XWiki.user2")))
            .thenReturn(Optional.of(dc4));

        when(this.resolver.resolve("user1")).thenReturn(user1Reference);
        when(this.resolver.resolve("user2")).thenReturn(user2Reference);
        when(this.serializer.serialize(user1Reference)).thenReturn("xwiki:XWiki.user1");
        when(this.serializer.serialize(user2Reference)).thenReturn("xwiki:XWiki.user2");

        List<DiscussionContext> discussionContexts =
            this.target.initializeContextUsers("Author", asList("user1", "user2", ""));

        assertEquals(4, discussionContexts.size());
        assertEquals(dc1, discussionContexts.get(0));
        assertEquals(dc2, discussionContexts.get(1));
        assertEquals(dc3, discussionContexts.get(2));
        assertEquals(dc4, discussionContexts.get(3));
    }

    @Test
    void initializeContextGroups() throws Exception
    {
        DiscussionContext dc1 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc1"), "", "",
            new DiscussionContextEntityReference("messagestream-emitter", "Author"));
        DiscussionContext dc2 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc2"), "", "",
            new DiscussionContextEntityReference("messagestream-user", "Author"));
        DiscussionContext dc3 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc3"), "", "",
            new DiscussionContextEntityReference("messagestream-user", "xwiki:XWiki.U1"));
        DiscussionContext dc4 = new DiscussionContext(
            new DiscussionContextReference(DISCUSSION_MESSAGESTREAM_HINT, "dc4"), "", "",
            new DiscussionContextEntityReference("messagestream-user", "xwiki:XWiki.U2"));
        DocumentReference g1DR = new DocumentReference("xwiki", "XWiki", "G1");
        DocumentReference g2DR = new DocumentReference("xwiki", "XWiki", "G2");
        DocumentReference u1DR = new DocumentReference("xwiki", "XWiki", "U1");
        DocumentReference u2DR = new DocumentReference("xwiki", "XWiki", "U2");

        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-emitter", "Author")))
            .thenReturn(Optional.of(dc1));
        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-user", "Author")))
            .thenReturn(Optional.of(dc2));
        when(this.resolver.resolve("G1")).thenReturn(g1DR);
        when(this.resolver.resolve("G2")).thenReturn(g2DR);
        when(this.groupManager.getMembers(g1DR, true)).thenReturn(asList(u1DR));
        when(this.groupManager.getMembers(g2DR, true)).thenReturn(asList(u1DR, u2DR));
        when(this.serializer.serialize(u1DR)).thenReturn("xwiki:XWiki.U1");
        when(this.serializer.serialize(u2DR)).thenReturn("xwiki:XWiki.U2");
        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-user", "xwiki:XWiki.U1")))
            .thenReturn(Optional.of(dc3));
        when(this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
            new DiscussionContextEntityReference("messagestream-user", "xwiki:XWiki.U2")))
            .thenReturn(Optional.of(dc4));

        List<DiscussionContext> discussionContexts =
            this.target.initializeContextGroups("Author", asList("G1", "", "G2"));
        assertEquals(4, discussionContexts.size());
        assertTrue(discussionContexts.contains(dc1));
        assertTrue(discussionContexts.contains(dc2));
        assertTrue(discussionContexts.contains(dc3));
        assertTrue(discussionContexts.contains(dc4));
    }
}