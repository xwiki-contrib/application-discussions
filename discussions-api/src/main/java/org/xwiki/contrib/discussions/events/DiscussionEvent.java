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

/**
 * Event sent when a discussion is changed.
 *
 * @version $Id$
 * @since 1.0
 */
public class DiscussionEvent implements DiscussionsEvent
{
    private final ActionType actionType;

    /**
     * Default constructor.
     *
     * @param actionType the action type of the event.
     */
    public DiscussionEvent(ActionType actionType)
    {
        this.actionType = actionType;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof DiscussionEvent && this.actionType == ((DiscussionEvent) otherEvent).actionType;
    }
}
