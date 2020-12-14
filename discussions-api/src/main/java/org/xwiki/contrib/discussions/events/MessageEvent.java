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
package org.xwiki.contrib.discussions.events;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.stability.Unstable;

/**
 * Event sent when a discussion context is changed.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
public class MessageEvent implements DiscussionsEvent
{
    private final ActionType actionType;

    /**
     * Default constructor.
     *
     * @param actionType the action type of the event.
     */
    public MessageEvent(ActionType actionType)
    {
        this.actionType = actionType;
    }

    /**
     * @return the action type of the event
     */
    public ActionType getActionType()
    {
        return this.actionType;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof MessageEvent
            && this.actionType == ((MessageEvent) otherEvent).actionType;
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

        MessageEvent that = (MessageEvent) o;

        return new EqualsBuilder()
            .append(this.actionType, that.actionType)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(this.actionType)
            .toHashCode();
    }
}
