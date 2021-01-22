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
package org.xwiki.contrib.discussions.test.ui;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.rest.DiscussionREST;
import org.xwiki.contrib.discussions.rest.model.CreateDiscussion;
import org.xwiki.contrib.discussions.test.po.DiscussionPage;
import org.xwiki.contrib.discussions.test.po.MessageBoxPage;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * UI tests of the Discussions application.
 */
@UITest
class DiscussionsIT
{
    public static final ObjectMapper OBJECT_MAPPER = new XmlMapper();

    private Discussion discussion;

    private DiscussionPage discussionPage;

    @Test
    @Order(1)
    void createDiscussion(TestUtils setup) throws Exception
    {
        setup.loginAsSuperAdmin();

        CreateDiscussion restObject = new CreateDiscussion()
            .setTitle("title")
            .setDescription("description");
        String s = OBJECT_MAPPER.writeValueAsString(restObject);
        URI createDiscussionURI = getCreateDiscussionURI(setup);
        PostMethod postMethod =
            setup.rest().executePost(createDiscussionURI, IOUtils.toInputStream(s,
                StandardCharsets.UTF_8));

        InputStream responseBodyAsStream = postMethod.getResponseBodyAsStream();
        Discussion discussion = OBJECT_MAPPER.readValue(responseBodyAsStream, Discussion.class);
        assertEquals(discussion.getTitle(), "title");
        assertEquals(discussion.getDescription(), "description");

        this.discussion = discussion;
    }

    private URI getCreateDiscussionURI(TestUtils setup)
    {
        URI uri = setup.rest().createUri(DiscussionREST.class, new HashMap<>());
        return URI.create(uri.toASCIIString() + "/discussion");
    }

    @Test
    @Order(2)
    void displayDiscussion(TestUtils setup, TestReference reference)
    {
        setup.gotoPage(new DocumentReference("xwiki", "XWiki", "Main"));
        this.discussionPage = DiscussionPage.createDiscussionPage(reference, this.discussion.getReference(),
            "discussionsns", 3, "testtype");

        assertEquals("title", this.discussionPage.getTitle());
        assertEquals("description", this.discussionPage.getDescription());
        assertEquals("No messages!", this.discussionPage.getNoMessagesText());

        this.discussionPage.sendNewMessage("New **message**");

        List<MessageBoxPage> messages = this.discussionPage.getMessages();
        assertEquals(1, messages.size());
        MessageBoxPage message = messages.get(0);

        assertEquals("superadmin", message.getAuthor());
        // the xwiki syntax is interpreted and the text is "new message" without the stars since message is in bold.
        assertEquals("New message", message.getMessageContent());
    }

    @Test
    @Order(3)
    void testPagination(TestUtils setup, TestReference reference)
    {
        // Creates enough messages to add a second page to the discussion.
        Stream.of(2, 3, 4).forEach(it -> {
            this.discussionPage.sendNewMessage("Message " + it);
        });

        // Check that we are in the second page with only the last message (since the number of elements per pages 
        // is 3).
        List<MessageBoxPage> messagesP2 = this.discussionPage.getMessages();
        assertEquals(1, messagesP2.size());

        MessageBoxPage messageP2 = messagesP2.get(0);
        assertEquals("superadmin", messageP2.getAuthor());
        assertEquals("Message 4", messageP2.getMessageContent());

        // Go to the previous page.
        this.discussionPage.goToPreviousPage();

        // Check the messages, the first message created in the previous step and the two first messages created above.
        List<MessageBoxPage> messagesP1 = this.discussionPage.getMessages();
        assertEquals(3, messagesP1.size());

        MessageBoxPage message1P1 = messagesP1.get(0);
        assertEquals("superadmin", message1P1.getAuthor());
        assertEquals("New message", message1P1.getMessageContent());
        MessageBoxPage message2P1 = messagesP1.get(1);
        assertEquals("superadmin", message2P1.getAuthor());
        assertEquals("Message 2", message2P1.getMessageContent());
        MessageBoxPage message3P1 = messagesP1.get(2);
        assertEquals("superadmin", message3P1.getAuthor());
        assertEquals("Message 3", message3P1.getMessageContent());
    }
}
