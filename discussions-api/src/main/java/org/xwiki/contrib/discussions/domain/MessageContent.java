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
import org.xwiki.rendering.syntax.Syntax;

/**
 * A message content and its syntax.
 *
 * @version $Id$
 * @since 1.0
 */
public class MessageContent
{
    private final String content;

    private final Syntax syntax;

    /**
     * Default constructor.
     *
     * @param content the content of the message
     * @param syntax the syntax of the content of the message
     */
    public MessageContent(String content, Syntax syntax)
    {
        this.content = content;
        this.syntax = syntax;
    }

    /**
     * @return the content of the message
     */
    public String getContent()
    {
        return this.content;
    }

    /**
     * @return the syntax of the content of the message
     */
    public Syntax getSyntax()
    {
        return this.syntax;
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

        MessageContent that = (MessageContent) o;

        return new EqualsBuilder()
            .append(this.content, that.content)
            .append(this.syntax, that.syntax)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(this.content)
            .append(this.syntax)
            .toHashCode();
    }
}
