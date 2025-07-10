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
package org.xwiki.contrib.discussions.store.meta;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.model.reference.LocalDocumentReference;

import static java.util.Arrays.asList;

/**
 * Provides the structural information about the discussion contexts.
 * <ul>
 *     <li>Class location</li>
 *     <li>Property names</li>
 *     <li>Property pretty names</li>
 * </ul>
 *
 * @version $Id$
 * @since 1.0
 */
public interface DiscussionContextMetadata
{
    /**
     * Reference property name.
     */
    String REFERENCE_NAME = "reference";

    /**
     * Reference property pretty name.
     */
    String REFERENCE_PRETTY_NAME = "Reference";

    /**
     * Entity reference type property name.
     */
    String ENTITY_REFERENCE_TYPE_NAME = "entityReferenceType";

    /**
     * Entity reference type property pretty name.
     */
    String ENTITY_REFERENCE_TYPE_PRETTY_NAME = "Entity Reference Type";

    /**
     * Entity reference property name.
     */
    String ENTITY_REFERENCE_NAME = "entityReference";

    /**
     * Entity reference property pretty name.
     */
    String ENTITY_REFERENCE_PRETTY_NAME = "Entity reference";

    /**
     * Name property name.
     */
    String NAME_NAME = "name";

    /**
     * Name property pretty name.
     */
    String NAME_PRETTY_NAME = "Name";

    /**
     * Description property name.
     */
    String DESCRIPTION_NAME = "description";

    /**
     * Description property pretty name.
     */
    String DESCRIPTION_PRETTY_NAME = "Description";

    /**
     * Creation date property name.
     */
    String CREATION_DATE_NAME = "creationDate";

    /**
     * Creation date property pretty name.
     */
    String CREATION_DATE_PRETTY_NAME = "Creation Date";

    /**
     * Update date property name.
     */
    String UPDATE_DATE_NAME = "updateDate";

    /**
     * Update date property pretty name.
     */
    String UPDATE_DATE_PRETTY_NAME = "Update Date";

    /**
     * States property name.
     */
    String STATES_NAME = "states";

    /**
     * States property pretty name.
     */
    String STATES_PRETTY_NAME = "States";

    /**
     * Pined property name.
     */
    String PINED_NAME = "pined";

    /**
     * Pined property pretty name.
     */
    String PINED_PRETTY_NAME = "Pined";

    /**
     * Discussions property name.
     */
    String DISCUSSIONS_NAME = "discussions";

    /**
     * Discussion context metadata key.
     * @since 2.2
     */
    String METADATA_KEY = "key";

    /**
     * Discussion context metadata value.
     * @since 2.2
     */
    String METADATA_VALUE = "value";

    /**
     * Constant to avoid string duplication.
     */
    String DISCUSSIONS_STR = "Discussions";

    /**
     * Discussions property pretty name.
     */
    String DISCUSSIONS_PRETTY_NAME = DISCUSSIONS_STR;

    /**
     * Spaces location for discussion code.
     */
    List<String> XCLASS_SPACES = asList(DISCUSSIONS_STR, "Code");

    /**
     * Name of the xclass.
     */
    String XCLASS_NAME = "DiscussionContextClass";

    /**
     * Name of the metadata xclass.
     */
    String METADATA_XCLASS_NAME = "DiscussionContextMetadataClass";

    /**
     * Reference for the xlcass.
     */
    LocalDocumentReference XCLASS_REFERENCE = new LocalDocumentReference(XCLASS_SPACES, XCLASS_NAME);

    /**
     * Format for the fullname class.
     */
    String FULLNAME_FORMAT = "%s.%s";
    /**
     * Serialized reference of the xclass.
     */
    String XCLASS_FULLNAME = String.format(FULLNAME_FORMAT, StringUtils.join(XCLASS_SPACES, '.'), XCLASS_NAME);

    /**
     * Reference for the metadata xclass.
     */
    LocalDocumentReference METADATA_XCLASS_REFERENCE = new LocalDocumentReference(XCLASS_SPACES, METADATA_XCLASS_NAME);

    /**
     * Serialized reference of the metadata xlcass.
     */
    String METADATA_XCLASS_FULLNAME =
        String.format(FULLNAME_FORMAT, StringUtils.join(XCLASS_SPACES, '.'), METADATA_XCLASS_NAME);
}
