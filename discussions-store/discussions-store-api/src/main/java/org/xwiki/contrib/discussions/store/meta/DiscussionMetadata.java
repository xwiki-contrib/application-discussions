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
 * Provides the structural information about the discussions.
 * <ul>
 *     <li>Class location</li>
 *     <li>Property names</li>
 *     <li>Property pretty names</li>
 * </ul>
 *
 * @version $Id$
 * @since 1.0
 */
public interface DiscussionMetadata
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
     * Title property name.
     */
    String TITLE_NAME = "title";

    /**
     * Title property pretty name.
     */
    String TITLE_PRETTY_NAME = "Title";

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
     * Discussion contexts property name.
     */
    String DISCUSSION_CONTEXTS_NAME = "discussionContexts";

    /**
     * Discussion contexts property pretty name.
     */
    String DISCUSSION_CONTEXTS_PRETTY_NAME = "Discussion Contexts";

    /**
     * Main Document property name.
     */
    String MAIN_DOCUMENT_NAME = "mainDocument";

    /**
     * Main Document property pretty name.
     */
    String MAIN_DOCUMENT_PRETTY_NAME = "Main Document";

    /**
     * XClass location.
     */
    List<String> XCLASS_SPACES = asList("Discussions", "Code");

    /**
     * XClass name.
     */
    String XCLASS_NAME = "DiscussionClass";

    /**
     * XClass reference.
     */
    LocalDocumentReference XCLASS_REFERENCE = new LocalDocumentReference(XCLASS_SPACES, XCLASS_NAME);

    /**
     * XClass serialized reference.
     */
    String XCLASS_FULLNAME = String.format("%s.%s", StringUtils.join(XCLASS_SPACES, '.'), XCLASS_NAME);
}
