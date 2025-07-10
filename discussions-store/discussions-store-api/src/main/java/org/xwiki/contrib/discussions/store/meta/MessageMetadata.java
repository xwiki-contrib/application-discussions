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
 * Provides the structural information about the message.
 * <ul>
 *     <li>Class location</li>
 *     <li>Property names</li>
 *     <li>Property pretty names</li>
 * </ul>
 *
 * @version $Id$
 * @since 1.0
 */
public interface MessageMetadata
{
    /**
     * Reference field name.
     */
    String REFERENCE_NAME = "reference";

    /**
     * Reference field pretty name.
     */
    String REFERENCE_PRETTY_NAME = "Reference";

    /**
     * Content field name.
     */
    String CONTENT_NAME = "content";

    /**
     * Content field pretty name.
     */
    String CONTENT_PRETTY_NAME = "Content";

    /**
     * Discussion reference field name.
     */
    String DISCUSSION_REFERENCE_NAME = "discussionReference";

    /**
     * Discussion reference field pretty name.
     */
    String DISCUSSION_REFERENCE_PRETTY_NAME = "Discussion Reference";

    /**
     * Author field name.
     */
    String AUTHOR_TYPE_NAME = "authorType";

    /**
     * Author field pretty name.
     */
    String AUTHOR_TYPE_PRETTY_NAME = "Author Type";

    /**
     * Author field name.
     */
    String AUTHOR_REFERENCE_NAME = "authorReference";

    /**
     * Author field pretty name.
     */
    String AUTHOR_REFERENCE_PRETTY_NAME = "Author Reference";

    /**
     * Update date field name.
     */
    String UPDATE_DATE_NAME = "updateDate";

    /**
     * Update date field pretty name.
     */
    String UPDATE_DATE_PRETTY_NAME = "Update Date";

    /**
     * Create date field name.
     */
    String CREATE_DATE_NAME = "createDate";

    /**
     * Create date field pretty name.
     */
    String CREATE_DATE_PRETTY_NAME = "Create Date";

    /**
     * States field name.
     */
    String STATES_NAME = "states";

    /**
     * States field pretty name.
     */
    String STATES_PRETTY_NAME = "States";

    /**
     * Reply to field name.
     */
    String REPLY_TO_NAME = "replyTo";

    /**
     * Reply to field pretty name.
     */
    String REPLY_TO_PRETTY_NAME = "Reply To";

    /**
     * Pined field name.
     */
    String PINED_NAME = "pined";

    /**
     * Pined field pretty name.
     */
    String PINED_PRETTY_NAME = "Pined";
    /**
     * Location of the xclass.
     */
    List<String> XCLASS_SPACES = asList("Discussions", "Code");

    /**
     * XClass name.
     */
    String XCLASS_NAME = "MessageClass";

    /**
     * XClass reference.
     */
    LocalDocumentReference XCLASS_REFERENCE = new LocalDocumentReference(XCLASS_SPACES, XCLASS_NAME);

    /**
     * Serialization of the xclass.
     */
    String XCLASS_FULLNAME = String.format("%s.%s", StringUtils.join(XCLASS_SPACES, '.'), XCLASS_NAME);
}
