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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
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
        setup.createPage(reference,
            "{{discussion reference=\"" + this.discussionReference + "\" namespace=\"discussionsns\"/}}\n",
            "view discussion");

        assertEquals("title", setup.getDriver().findElement(By.cssSelector("h2")).getText());
        assertEquals("description", setup.getDriver().findElement(By.cssSelector("h2 + p")).getText());
        assertEquals("No messages!", setup.getDriver().findElement(By.cssSelector("h3 + p")).getText());

        setup.getDriver().findElement(By.id("content")).sendKeys("New message");
        setup.getDriver().findElement(By.cssSelector("button.message-submit-button")).click();

        List<WebElement> messages = setup.getDriver().findElements(By.cssSelector("div.box"));
        assertEquals(1, messages.size());
        WebElement message = messages.get(0);
        List<WebElement> paragraphs = message.findElements(By.cssSelector(".col-xs-11 > p"));
        assertEquals("xwiki:XWiki.superadmin", paragraphs.get(0).getText());
        assertEquals("New message", paragraphs.get(1).getText());
    }
}
