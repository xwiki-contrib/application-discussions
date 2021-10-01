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
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.stability.Unstable;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Definition of the discussion context class.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
public class DiscussionContext
{
    private final DiscussionContextReference reference;

    private final String name;

    private final String description;

    private final DiscussionContextEntityReference entityReference;

    /**
     * Default constructor.
     *
     * @param reference the discussion context unique reference
     * @param name the discussion context name
     * @param description the discussion context description
     * @param entityReference the entity reference
     */
    public DiscussionContext(DiscussionContextReference reference, String name, String description,
        DiscussionContextEntityReference entityReference)
    {
        this.reference = reference;
        this.name = name;
        this.description = description;
        this.entityReference = entityReference;
    }

    /**
     * @return the discussion context unique reference
     */
    public DiscussionContextReference getReference()
    {
        return this.reference;
    }

    /**
     * @return the discussion context name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the discussion context description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return the entity reference
     */
    public DiscussionContextEntityReference getEntityReference()
    {
        return this.entityReference;
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

        DiscussionContext that = (DiscussionContext) o;

        return new EqualsBuilder()
            .append(this.reference, that.reference)
            .append(this.name, that.name)
            .append(this.description, that.description)
            .append(this.entityReference, that.entityReference)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(this.reference)
            .append(this.name)
            .append(this.description)
            .append(this.entityReference)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("reference", this.getReference())
            .append("name", this.getName())
            .append("description", this.getDescription())
            .append("entityReference", this.getEntityReference())
            .build();
    }
}
