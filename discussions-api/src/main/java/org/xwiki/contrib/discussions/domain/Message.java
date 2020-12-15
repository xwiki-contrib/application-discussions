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

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.syntax.Syntax;
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

    private final String actorReference;

    private final String actorType;

    private final Date createDate;

    private final Date updateDate;

    private final Discussion discussion;

    private final MessageContent messageContent;

    /**
     * Default constructor.
     *
     * @param reference the reference
     * @param messageContent the content of the message and its syntax
     * @param actorType the actor type
     * @param actorReference the actor reference
     * @param createDate date of creation of the message
     * @param updateDate date of the last update of the message
     * @param discussion the discussion of the message
     */
    public Message(String reference, MessageContent messageContent, String actorType, String actorReference,
        Date createDate,
        Date updateDate, Discussion discussion)
    {
        this.reference = reference;
        this.messageContent = messageContent;
        this.actorType = actorType;
        this.actorReference = actorReference;
        this.createDate = createDate;
        this.updateDate = updateDate;
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
        return this.messageContent.getContent();
    }

    /**
     * @return this actor reference
     */
    public String getActorReference()
    {
        return this.actorReference;
    }

    /**
     * @return the actor type
     */
    public String getActorType()
    {
        return this.actorType;
    }

    /**
     * @return the date of creation of the message
     */
    public Date getCreateDate()
    {
        return this.createDate;
    }

    /**
     * @return the date of update of the message
     */
    public Date getUpdateDate()
    {
        return this.updateDate;
    }

    /**
     * @return the discussion of the message.
     */
    public Discussion getDiscussion()
    {
        return this.discussion;
    }

    /**
     * @return the syntax of the message content
     */
    public Syntax getSyntax()
    {
        return this.messageContent.getSyntax();
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
            .append(this.reference, message.reference)
            .append(this.messageContent, message.messageContent)
            .append(this.actorReference, message.actorReference)
            .append(this.actorType, message.actorType)
            .append(this.createDate, message.createDate)
            .append(this.updateDate, message.updateDate)
            .append(this.discussion, message.discussion)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(this.reference)
            .append(this.messageContent)
            .append(this.actorReference)
            .append(this.actorType)
            .append(this.createDate)
            .append(this.updateDate)
            .append(this.discussion)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("reference", this.getReference())
            .append("content", this.getContent())
            .append("syntax", this.getSyntax())
            .append("actorType", this.getActorType())
            .append("actorReference", this.getActorReference())
            .append("createDate", this.getCreateDate())
            .append("updateDate", this.getUpdateDate())
            .append("discussion", this.getDiscussion())
            .build();
    }
}

