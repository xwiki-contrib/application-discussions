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
package org.xwiki.contrib.discussions.domain.references;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.stability.Unstable;

/**
 * Represents the reference of an actor based on a type of actor and its reference in this type.
 *
 * @version $Id$
 * @since 2.0
 */
@Unstable
public class ActorReference
{
    private String type;
    private String reference;

    /**
     * Default constructor.
     *
     * @param type the type of actor
     * @param reference the actual reference of the actor.
     */
    public ActorReference(String type, String reference)
    {
        this.type = type;
        this.reference = reference;
    }

    /**
     * @return the type of actor.
     */
    public String getType()
    {
        return type;
    }

    /**
     * @return the actual reference of the actor.
     */
    public String getReference()
    {
        return reference;
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

        ActorReference that = (ActorReference) o;

        return new EqualsBuilder().append(type, that.type)
            .append(reference, that.reference).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(type).append(reference).toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("type", type)
            .append("reference", reference)
            .toString();
    }
}
