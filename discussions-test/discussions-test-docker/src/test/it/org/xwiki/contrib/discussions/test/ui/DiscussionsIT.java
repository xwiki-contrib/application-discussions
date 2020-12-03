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

import java.util.List;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.test.po.DiscussionPage;
import org.xwiki.contrib.discussions.test.po.MessageBoxPage;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.ViewPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UI tests of the Discussions application.
 */
@UITest
class DiscussionsIT
{
    private String discussionReference;

    @Test
    @Order(1)
    void createDiscussion(TestUtils setup, TestReference reference)
    {
        setup.loginAsSuperAdmin();
        // TODO: update once the rest server is developed.
        ViewPage discussion = setup.createPage(reference, "{{velocity}}"
            + "$services.discussions.createDiscussion('title', 'description')"
            + "{{/velocity}}", "discussion");

        String content = discussion.getContent();
        this.discussionReference = content.substring(13).split("]")[0];
        assertTrue(this.discussionReference.startsWith("title-"));
    }

    @Test
    @Order(2)
    void displayDiscussion(TestUtils setup, TestReference reference)
    {
        DiscussionPage discussionPage = DiscussionPage.createDiscussionPage(reference, this.discussionReference,
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
