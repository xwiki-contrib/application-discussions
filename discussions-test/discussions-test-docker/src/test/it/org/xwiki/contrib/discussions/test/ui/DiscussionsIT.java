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
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    @Test
    @Order(1)
    void createDiscussion(TestUtils setup) throws Exception
    {
        setup.loginAsSuperAdmin();

        CreateDiscussion restObject = new CreateDiscussion()
            .setTitle("title")
            .setDescription("description");
        String s = OBJECT_MAPPER.writeValueAsString(restObject);
        PostMethod postMethod = setup.rest().executePost(DiscussionREST.class, IOUtils.toInputStream(s,
            StandardCharsets.UTF_8));

        InputStream responseBodyAsStream = postMethod.getResponseBodyAsStream();
        Discussion discussion = OBJECT_MAPPER.readValue(responseBodyAsStream, Discussion.class);
        assertEquals(discussion.getTitle(), "title");
        assertEquals(discussion.getDescription(), "description");

        this.discussion = discussion;
    }

    @Test
    @Order(2)
    void displayDiscussion(TestUtils setup, TestReference reference)
    {
        setup.gotoPage(new DocumentReference("xwiki", "XWiki", "Main"));
        DiscussionPage discussionPage = DiscussionPage.createDiscussionPage(reference, this.discussion.getReference(),
            "discussionsns");

        assertEquals("title", discussionPage.getTitle());
        assertEquals("description", discussionPage.getDescription());
        assertEquals("No messages!", discussionPage.getNoMessagesText());

        discussionPage.sendNewMessage("New message");

        List<MessageBoxPage> messages = discussionPage.getMessages();
        assertEquals(1, messages.size());
        MessageBoxPage message = messages.get(0);

        assertEquals("xwiki:XWiki.superadmin", message.getAuthor());
        assertEquals("New message", message.getMessageContent());
    }
}
