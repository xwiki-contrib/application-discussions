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
package org.xwiki.contrib.discussions.rest;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Hold the information of a row of a live table for the discussions.
 *
 * @version $Id$
 * @since 1.0
 */
public class DiscussionLiveTableRow
{
    private String title;

    private String titleUrl;

    private Date updateDate;

    private long messageCount;

    /**
     * @return the discussion title
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * @param title the discussion title
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the title URL
     */
    @JsonProperty("title_url")
    public String getTitleUrl()
    {
        return this.titleUrl;
    }

    /**
     * Indicates if the field is viewable. In our context the fields are pre-filtered and are all visible at this
     * point.
     * <p>
     * Once https://jira.xwiki.org/browse/XWIKI-15552 fixed, this field will not required.
     *
     * @return the constant {@code true}
     */
    @JsonProperty("doc_viewable")
    public boolean docViewable()
    {
        return true;
    }

    /**
     * @param titleUrl the title url
     */
    public void setTitleUrl(String titleUrl)
    {
        this.titleUrl = titleUrl;
    }

    /**
     * @return the update date of the discussion
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd hh:mm")
    public Date getUpdateDate()
    {
        return this.updateDate;
    }

    /**
     * @param updateDate the update date of the discussion
     */
    public void setUpdateDate(Date updateDate)
    {
        this.updateDate = updateDate;
    }

    /**
     * @return the number of messages of the discussion
     */
    public long getMessageCount()
    {
        return this.messageCount;
    }

    /**
     * @param messageCount the number of messages of the discussion
     */
    public void setMessageCount(long messageCount)
    {
        this.messageCount = messageCount;
    }
}
