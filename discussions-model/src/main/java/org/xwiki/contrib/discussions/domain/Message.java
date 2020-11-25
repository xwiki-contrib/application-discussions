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

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.stability.Unstable;

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

    private final DocumentReference author;

    private Discussion discussion;

    /**
     * Default constructor.
     *
     * @param reference the reference
     * @param content the content
     * @param author the author
     * @param discussion the discussion of the message
     */
    public Message(String reference, String content, DocumentReference author, Discussion discussion)
    {
        this.reference = reference;
        this.content = content;
        this.author = author;
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
     * @return the author
     */
    public DocumentReference getAuthor()
    {
        return this.author;
    }

    /**
     * @return the discussion of the message.
     */
    public Discussion getDiscussion()
    {
        return this.discussion;
    }
}

