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
package org.xwiki.contrib.discussions.internal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.resource.AbstractResourceReference;
import org.xwiki.resource.ResourceType;

/**
 * Holds a discussions resource reference.
 *
 * @version $Id$
 * @since 1.0
 */
public class DiscussionsResourceReference extends AbstractResourceReference
{
    private final DiscussionsEntityType discussionsEntityType;

    private final DiscussionsActionType actionType;

    /**
     * Default constructor.
     *
     * @param actionType the discussions action type
     * @param discussionsEntityType the discussions entity type
     */
    public DiscussionsResourceReference(DiscussionsActionType actionType, DiscussionsEntityType discussionsEntityType)
    {
        this.setType(new ResourceType("discussions"));
        this.actionType = actionType;
        this.discussionsEntityType = discussionsEntityType;
    }

    /**
     * @return the discussions action type
     */
    public DiscussionsActionType getActionType()
    {
        return this.actionType;
    }

    /**
     * @return the discussions entity type
     */
    public DiscussionsEntityType getDiscussionsEntityType()
    {
        return this.discussionsEntityType;
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

        DiscussionsResourceReference that = (DiscussionsResourceReference) o;

        return new EqualsBuilder()
            .appendSuper(super.equals(o))
            .append(this.discussionsEntityType, that.discussionsEntityType)
            .append(this.actionType, that.actionType)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(this.discussionsEntityType)
            .append(this.actionType)
            .toHashCode();
    }
}
