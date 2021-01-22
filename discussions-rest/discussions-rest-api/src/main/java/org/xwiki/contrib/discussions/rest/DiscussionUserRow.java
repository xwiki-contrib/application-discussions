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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds the information of a row in the user list table.
 *
 * @version $Id$
 * @since 1.0
 */
public class DiscussionUserRow
{
    private String name;

    private String link;

    /**
     * Default constructor.
     *
     * @param name the name of the user
     * @param link the link to the user's profile
     */
    public DiscussionUserRow(String name, String link)
    {

        this.name = name;
        this.link = link;
    }

    /**
     * @return the name of the user
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the name of the user
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the link to the user's profile
     */
    @JsonProperty("name_url")
    public String getLink()
    {
        return this.link;
    }

    /**
     * @param link the link to the user's profile
     */
    public void setLink(String link)
    {
        this.link = link;
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
}
