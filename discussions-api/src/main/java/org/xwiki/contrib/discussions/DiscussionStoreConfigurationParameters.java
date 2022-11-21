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
package org.xwiki.contrib.discussions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.stability.Unstable;

/**
 * Parameters that are used for storage configuration.
 *
 * @version $Id$
 * @since 2.0
 */
@Unstable
public class DiscussionStoreConfigurationParameters extends LinkedHashMap<String, Object>
{
    /**
     * Key of the configuration listing the attachments temporary uploaded as part of a message creation.
     *
     * @since 2.1
     */
    @Unstable
    public static final String TEMPORARY_UPLOADED_ATTACHMENTS = "temporaryUploadedAttachments";

    /**
     * Default constructor.
     */
    public DiscussionStoreConfigurationParameters()
    {
        super();
    }

    /**
     * Constructor using a pre-defined list of parameters.
     *
     * @param parameters data to copy in the current instance.
     */
    public DiscussionStoreConfigurationParameters(Map<String, Object> parameters)
    {
        super(parameters);
    }
}
