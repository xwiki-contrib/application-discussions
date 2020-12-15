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
package org.xwiki.contrib.discussions.test.po;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.ui.po.ViewPage;

/**
 * Represents a discussion page.
 *
 * @version $Id$
 * @since 1.0
 */
public final class DiscussionPage extends ViewPage
{
    @FindBy(css = "h2")
    private WebElement title;

    @FindBy(css = "h2 + p")
    private WebElement description;

    // Elements of the send message form
    @FindBy(id = "content")
    private WebElement contentField;

    @FindBy(css = "button.message-submit-button")
    private WebElement addMessageButton;

    @FindBy(css = "div.box")
    private List<WebElement> messageBoxes;

    @FindBy(css = "a.prevPagination")
    private WebElement previousPageLink;

    /**
     * Default constructor.
     *
     * @param reference the reference of the page of the discussion
     * @param discussionReference the discussion reference
     * @param namespace the discussion macro namespace
     */
    private DiscussionPage(DocumentReference reference, String discussionReference, String namespace, long pageSize)
    {
        getUtil().createPage(reference,
            String.format("{{discussion reference=\"%s\" namespace=\"%s\" pageSize=\"%s\"/}}", discussionReference,
                namespace, pageSize),
            "view discussion");
    }

    /**
     * Creates a page to display a discussion.
     *
     * @param reference reference of the page
     * @param discussionReference reference of the discussion
     * @param namespace namespace of the discussion macro
     * @param pageSize the number of messages on a page
     * @return the resulting page object
     */
    public static DiscussionPage createDiscussionPage(DocumentReference reference, String discussionReference,
        String namespace, long pageSize)
    {
        return new DiscussionPage(reference, discussionReference, namespace, pageSize);
    }

    /**
     * @return the text of the title of the discussion
     */
    public String getTitle()
    {
        return this.title.getText();
    }

    /**
     * @return the text of the description of the discussion
     */
    public String getDescription()
    {
        return this.description.getText();
    }

    /**
     * @return the text displayed when the discussion has no messages.
     */
    public String getNoMessagesText()
    {
        return getDriver().findElementWithoutWaiting(By.cssSelector("h3 + p")).getText();
    }

    /**
     * @param content the content of the new message
     */
    public void sendNewMessage(String content)
    {
        this.contentField.sendKeys(content);
        this.addMessageButton.click();
    }

    /**
     * @return the list of message boxes of the current page
     */
    public List<MessageBoxPage> getMessages()
    {
        return this.messageBoxes.stream()
            .map(MessageBoxPage::new)
            .collect(Collectors.toList());
    }

    /**
     * Use the pagination to go to the previous page.
     */
    public void goToPreviousPage()
    {
        this.previousPageLink.click();
    }
}
