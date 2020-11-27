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
 * Definition of the message class.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
public class Message
{
    private final String reference;

    private final String content;

    private final String actorReference;

    private final String actorType;

    private final Discussion discussion;

    /**
     * Default constructor.
     *
     * @param reference the reference
     * @param content the content
     * @param actorType the actor type
     * @param actorReference the actor reference
     * @param discussion the discussion of the message
     */
    public Message(String reference, String content, String actorType, String actorReference, Discussion discussion)
    {
        this.reference = reference;
        this.content = content;
        this.actorType = actorType;
        this.actorReference = actorReference;
        this.discussion = discussion;
    }

    /**
     * @return the reference
     */
    public String getReference()
    {
        return this.reference;
    }

    /**
     * @return the content
     */
    public String getContent()
    {
        return this.content;
    }

    /**
     * @return this actor reference
     */
    public String getActorReference()
    {
        return actorReference;
    }

    /**
     * @return the actor type
     */
    public String getActorType()
    {
        return actorType;
    }

    /**
     * @return the discussion of the message.
     */
    public Discussion getDiscussion()
    {
        return this.discussion;
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

        Message message = (Message) o;

        return new EqualsBuilder()
            .append(reference, message.reference)
            .append(content, message.content)
            .append(actorReference, message.actorReference)
            .append(actorType, message.actorType)
            .append(discussion, message.discussion)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(reference)
            .append(content)
            .append(actorReference)
            .append(actorType)
            .append(discussion)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("reference", this.getReference())
            .append("content", this.getContent())
            .append("actorType", this.getActorType())
            .append("actorReference", this.getActorReference())
            .append("discussion", this.getDiscussion())
            .build();
    }
}

