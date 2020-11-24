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

import org.xwiki.stability.Unstable;

/**
 * Definition of the discussion context class.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
public class DiscussionContext
{
    private final String reference;

    private final String name;

    private final String description;

    private final String referenceType;

    private final String entityReference;

    /**
     * Default constructor.
     *
     * @param reference the discussion context unique reference
     * @param name the discussion context name
     * @param description the discussion context description
     * @param referenceType the reference type of the entity of the discussion context
     * @param entityReference the reference of the entity of the discussion context
     */
    public DiscussionContext(String reference, String name, String description, String referenceType,
        String entityReference)
    {

        this.reference = reference;
        this.name = name;
        this.description = description;
        this.referenceType = referenceType;
        this.entityReference = entityReference;
    }

    /**
     * @return the discussion context unique reference
     */
    public String getReference()
    {
        return this.reference;
    }

    /**
     * @return the discussion context name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the discussion context description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return the reference type of the entity of the discussion context
     */
    public String getReferenceType()
    {
        return this.referenceType;
    }

    /**
     * @return the reference of the entity of the discussion context
     */
    public String getEntityReference()
    {
        return this.entityReference;
    }
}
