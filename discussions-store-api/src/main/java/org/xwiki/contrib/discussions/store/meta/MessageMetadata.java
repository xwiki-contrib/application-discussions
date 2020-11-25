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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;

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
@Component(roles = { MessageMetadata.class })
@Singleton
@Unstable
public class MessageMetadata
{
    /**
     * Reference field name.
     */
    public static final String REFERENCE_NAME = "reference";

    /**
     * Reference field pretty name.
     */
    public static final String REFERENCE_PRETTY_NAME = "Reference";

    /**
     * Content field name.
     */
    public static final String CONTENT_NAME = "content";

    /**
     * Content field pretty name.
     */
    public static final String CONTENT_PRETTY_NAME = "Content";

    /**
     * Discussion reference field name.
     */
    public static final String DISCUSSION_REFERENCE_NAME = "discussionReference";

    /**
     * Discussion reference field pretty name.
     */
    public static final String DISCUSSION_REFERENCE_PRETTY_NAME = "Discussion Reference";

    /**
     * Author field name.
     */
    public static final String AUTHOR_NAME = "author";

    /**
     * Author field pretty name.
     */
    public static final String AUTHOR_PRETTY_NAME = "Author";

    /**
     * Update date field name.
     */
    public static final String UPDATE_DATE_NAME = "updateDate";

    /**
     * Update date field pretty name.
     */
    public static final String UPDATE_DATE_PRETTY_NAME = "Update Date";

    /**
     * Create date field name.
     */
    public static final String CREATE_DATE_NAME = "createDate";

    /**
     * Create date field pretty name.
     */
    public static final String CREATE_DATE_PRETTY_NAME = "Create Date";

    /**
     * States field name.
     */
    public static final String STATES_NAME = "states";

    /**
     * States field pretty name.
     */
    public static final String STATES_PRETTY_NAME = "States";

    /**
     * Reply to field name.
     */
    public static final String REPLY_TO_NAME = "replyTo";

    /**
     * Reply to field pretty name.
     */
    public static final String REPLY_TO_PRETTY_NAME = "Reply To";

    /**
     * Pined field name.
     */
    public static final String PINED_NAME = "pined";

    /**
     * Pined field pretty name.
     */
    public static final String PINED_PRETTY_NAME = "Pined";

    private static final String DISCUSSIONS_SPACE = "Discussions";

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    /**
     * @return the {@link DocumentReference} of the document holding the message XClass.
     */
    public EntityReference getMessageXClass()
    {
        return new DocumentReference(this.xcontextProvider.get().getMainXWiki(), asList(DISCUSSIONS_SPACE, "Code"),
            "MessageClass");
    }
}
