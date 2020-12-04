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
package org.xwiki.contrib.discussions.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.stability.Unstable;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Definition of the discussion class.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
public class Discussion
{
    private String reference;

    private String title;

    private String description;

    /**
     * Default constructor.
     *
     * @param reference the reference
     * @param title the title
     * @param description the description
     */
    public Discussion(String reference, String title, String description)
    {
        this.reference = reference;
        this.title = title;
        this.description = description;
    }

    /**
     * Empty constructor.
     */
    public Discussion()
    {
    }

    /**
     * @return the reference
     */
    public String getReference()
    {
        return this.reference;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @param reference the reference
     */
    public void setReference(String reference)
    {
        this.reference = reference;
    }

    /**
     * @param title the title
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @param description the description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Discussion that = (Discussion) o;

        return new EqualsBuilder()
            .append(this.reference, that.reference)
            .append(this.title, that.title)
            .append(this.description, that.description)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(this.reference)
            .append(this.title)
            .append(this.description)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("reference", this.getReference())
            .append("title", this.getTitle())
            .append("description", this.getDescription())
            .build();
    }
}
