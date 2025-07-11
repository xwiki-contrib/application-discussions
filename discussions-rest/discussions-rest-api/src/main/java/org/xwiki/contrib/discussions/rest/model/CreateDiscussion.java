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
package org.xwiki.contrib.discussions.rest.model;

import java.util.Map;

/**
 * Object used to wrap the values required to create a discussion.
 *
 * @version $Id$
 * @since 1.0
 */
public class CreateDiscussion
{
    private String applicationHint;

    private String title;

    private String description;

    private String mainDocument;

    private Map<String, Object> storeConfigurationParameters;

    /**
     * @return the title
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * @param title the title
     * @return the current object
     */
    public CreateDiscussion setTitle(String title)
    {
        this.title = title;
        return this;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @param description the description
     * @return the current object
     */
    public CreateDiscussion setDescription(String description)
    {
        this.description = description;
        return this;
    }

    /**
     * @return the main document to view the discussion
     */
    public String getMainDocument()
    {
        return this.mainDocument;
    }

    /**
     * @param mainDocument the main document to view the discussion
     * @return the current object
     */
    public CreateDiscussion setMainDocument(String mainDocument)
    {
        this.mainDocument = mainDocument;
        return this;
    }

    /**
     * @return the application used for creating the discussion
     * @since 2.0
     */
    public String getApplicationHint()
    {
        return applicationHint;
    }

    /**
     * @param applicationHint the application used for creating the discussion
     * @return the current instance
     * @since 2.0
     */
    public CreateDiscussion setApplicationHint(String applicationHint)
    {
        this.applicationHint = applicationHint;
        return this;
    }

    /**
     * @return parameters used for configuration store.
     * @since 2.0
     */
    public Map<String, Object> getStoreConfigurationParameters()
    {
        return storeConfigurationParameters;
    }

    /**
     * @param storeConfigurationParameters the parameters used for configuration store.
     * @return the current instance.
     * @since 2.0
     */
    public CreateDiscussion setStoreConfigurationParameters(Map<String, Object> storeConfigurationParameters)
    {
        this.storeConfigurationParameters = storeConfigurationParameters;
        return this;
    }
}
