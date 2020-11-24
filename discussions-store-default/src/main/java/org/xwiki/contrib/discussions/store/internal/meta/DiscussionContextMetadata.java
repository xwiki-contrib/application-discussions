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
package org.xwiki.contrib.discussions.store.internal.meta;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;

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
@Component(roles = { DiscussionContextMetadata.class })
@Singleton
public class DiscussionContextMetadata
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
     * Entity reference type property name.
     */
    public static final String ENTITY_REFERENCE_TYPE_NAME = "entityReferenceType";

    /**
     * Entity reference type property pretty name.
     */
    public static final String ENTITY_REFERENCE_TYPE_PRETTY_NAME = "Entity Reference Type";

    /**
     * Entity reference property name.
     */
    public static final String ENTITY_REFERENCE_NAME = "entityReference";

    /**
     * Entity reference property pretty name.
     */
    public static final String ENTITY_REFERENCE_PRETTY_NAME = "Entity reference";

    /**
     * Name property name.
     */
    public static final String NAME_NAME = "name";

    /**
     * Name property pretty name.
     */
    public static final String NAME_PRETTY_NAME = "Name";

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
     * Discussions property name.
     */
    public static final String DISCUSSIONS_NAME = "discussions";

    /*
     * Constant to avoid string duplication.
     */
    private static final String DISCUSSIONS_STR = "Discussions";

    /**
     * Discussions property pretty name.
     */
    public static final String DISCUSSIONS_PRETTY_NAME = DISCUSSIONS_STR;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    /**
     * @return the {@link DocumentReference} of the document hold the discussion context XClass.
     */
    public DocumentReference getDiscussionContextXClass()
    {
        return new DocumentReference(this.xcontextProvider.get().getMainXWiki(), asList(DISCUSSIONS_STR, "Code"),
            "DiscussionContextClass");
    }
}
