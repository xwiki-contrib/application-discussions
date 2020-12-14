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

import java.net.URI;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.stability.Unstable;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Describes the properties of an actor.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
public class ActorDescriptor
{
    private String name;

    private URI link;

    /**
     * @return the actor name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the actor name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the link to the actor profile
     */
    public URI getLink()
    {
        return this.link;
    }

    /**
     * @param link the link to the actor profile
     */
    public void setLink(URI link)
    {
        this.link = link;
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

        ActorDescriptor that = (ActorDescriptor) o;

        return new EqualsBuilder()
            .append(this.name, that.name)
            .append(this.link, that.link)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(this.name)
            .append(this.link)
            .toHashCode();
    }

    @Override public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("name", this.getName())
            .append("link", this.getLink())
            .build();
    }
}
