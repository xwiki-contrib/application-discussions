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
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;

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
@Component(roles = { DiscussionMetadata.class })
@Singleton
@Unstable
public class DiscussionMetadata
{
    /**
     * Reference property name.
     */
    public static final String REFERENCE_NAME = "reference";

    /**
     * Reference property pretty name.
     */
    public static final String REFERENCE_PRETTY_NAME = "Reference";

    /**
     * Title property name.
     */
    public static final String TITLE_NAME = "title";

    /**
     * Title property pretty name.
     */
    public static final String TITLE_PRETTY_NAME = "Title";

    /**
     * Description property name.
     */
    public static final String DESCRIPTION_NAME = "description";

    /**
     * Description property pretty name.
     */
    public static final String DESCRIPTION_PRETTY_NAME = "Description";

    /**
     * Creation date property name.
     */
    public static final String CREATION_DATE_NAME = "creationDate";

    /**
     * Creation date property pretty name.
     */
    public static final String CREATION_DATE_PRETTY_NAME = "Creation Date";

    /**
     * Update date property name.
     */
    public static final String UPDATE_DATE_NAME = "updateDate";

    /**
     * Update date property pretty name.
     */
    public static final String UPDATE_DATE_PRETTY_NAME = "Update Date";

    /**
     * States property name.
     */
    public static final String STATES_NAME = "states";

    /**
     * States property pretty name.
     */
    public static final String STATES_PRETTY_NAME = "States";

    /**
     * Pined property name.
     */
    public static final String PINED_NAME = "pined";

    /**
     * Pined property pretty name.
     */
    public static final String PINED_PRETTY_NAME = "Pined";

    /**
     * Discussion contexts property name.
     */
    public static final String DISCUSSION_CONTEXTS_NAME = "discussionContexts";

    /**
     * Discussion contexts property pretty name.
     */
    public static final String DISCUSSION_CONTEXTS_PRETTY_NAME = "Discussion Contexts";

    private static final String DISCUSSIONS_SPACE = "Discussions";

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    /**
     * @return the {@link DocumentReference} of the document holding the discussion XClass.
     */
    public EntityReference getDiscussionXClass()
    {
        return new DocumentReference(this.xcontextProvider.get().getMainXWiki(), asList(DISCUSSIONS_SPACE, "Code"),
            "DiscussionClass");
    }

    /**
     * @return the discussion storage space.
     */
    public SpaceReference getDiscussionSpace()
    {
        return new SpaceReference(this.xcontextProvider.get().getMainXWiki(), asList(DISCUSSIONS_SPACE, "Discussion"));
    }
}
