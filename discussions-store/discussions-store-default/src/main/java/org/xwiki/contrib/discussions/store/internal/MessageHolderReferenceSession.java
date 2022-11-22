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
package org.xwiki.contrib.discussions.store.internal;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.model.reference.DocumentReference;

/**
 * Simple POJO in charge of holding message holder reference information for a given
 * {@link javax.servlet.http.HttpSession}.
 * It implements {@link HttpSessionBindingListener} as instances are stored as attribute of
 * {@link javax.servlet.http.HttpSession} and we might want in the future to perform operations whenever the
 * session is invalidated.
 * Implementations inspired from {@code org.xwiki.store.filesystem.internal.TemporaryAttachmentSession}.
 *
 * @version $Id$
 * @since 2.1
 */
public class MessageHolderReferenceSession implements HttpSessionBindingListener, Serializable
{
    private final String sessionId;

    private final Map<DiscussionReference, DocumentReference> messageHolders;

    /**
     * Default constructor.
     *
     * @param sessionId identifier of the {@link javax.servlet.http.HttpSession}.
     */
    public MessageHolderReferenceSession(String sessionId)
    {
        this.sessionId = sessionId;
        this.messageHolders = new ConcurrentHashMap<>();
    }

    /**
     * @return the map of message holders information.
     */
    public Map<DiscussionReference, DocumentReference> getMessageHolders()
    {
        return messageHolders;
    }

    /**
     * @return the session identifier.
     */
    public String getSessionId()
    {
        return sessionId;
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

        MessageHolderReferenceSession that = (MessageHolderReferenceSession) o;

        return new EqualsBuilder()
            .append(sessionId, that.sessionId)
            .append(messageHolders, that.messageHolders)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 69).append(sessionId).append(messageHolders).toHashCode();
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event)
    {
        // Nothing to do.
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event)
    {
        // Nothing to do.
    }
}
