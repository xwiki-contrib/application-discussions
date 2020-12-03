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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.ViewPage;

/**
 * Represent a message box, displayed for instance in the list of messages of a discussion.
 *
 * @version $Id$
 * @since 1.0
 */
public class MessageBoxPage extends ViewPage
{
    private final WebElement root;

    /**
     * Default constructor.
     *
     * @param root the root element of the message box
     */
    public MessageBoxPage(WebElement root)
    {
        this.root = root;
    }

    /**
     * @return the author of the message
     */
    public String getAuthor()
    {
        return getParagraphs().get(0).getText();
    }

    /**
     * @return the message content
     */
    public String getMessageContent()
    {
        return getParagraphs().get(1).getText();
    }

    private List<WebElement> getParagraphs()
    {
        return getDriver().findElementsWithoutWaiting(this.root, By.cssSelector(".col-xs-11 > p"));
    }
}
