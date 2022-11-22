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

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.stability.Unstable;

/**
 * Represents a reference in different discussion obects.
 * A reference is always based on a pair containing an application hint specifying where the reference is used,
 * and the actual unique reference in that application. At the date of the creation of this class, the application
 * hint is mainly used to define where the objects are actually stored.
 *
 * @version $Id$
 * @since 2.0
 */
@Unstable
public abstract class AbstractDiscussionReference implements Serializable
{
    private final String applicationHint;
    private final String reference;

    /**
     * Default constructor.
     *
     * @param applicationHint the application in which the reference is used or created.
     * @param reference the actual internal reference.
     */
    public AbstractDiscussionReference(String applicationHint, String reference)
    {
        this.applicationHint = applicationHint;
        this.reference = reference;
    }

    /**
     * @return the application in which the reference is used.
     */
    public String getApplicationHint()
    {
        return applicationHint;
    }

    /**
     * @return the unique reference in the given application.
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

        AbstractDiscussionReference that = (AbstractDiscussionReference) o;

        return new EqualsBuilder().append(applicationHint, that.applicationHint)
            .append(reference, that.reference).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(applicationHint).append(reference).toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("applicationHint", applicationHint)
            .append("reference", reference)
            .toString();
    }
}
