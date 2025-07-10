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
package org.xwiki.contrib.discussions.store.internal.initializer;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;

import static com.xpn.xwiki.objects.classes.ListClass.DISPLAYTYPE_INPUT;
import static com.xpn.xwiki.objects.classes.ListClass.FREE_TEXT_ALLOWED;
import static com.xpn.xwiki.objects.classes.TextAreaClass.EditorType.WYSIWYG;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.CREATION_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.CREATION_DATE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.DESCRIPTION_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.DISCUSSIONS_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.DISCUSSIONS_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_TYPE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.NAME_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.NAME_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.PINED_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.PINED_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.REFERENCE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.STATES_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.STATES_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.UPDATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.UPDATE_DATE_PRETTY_NAME;

/**
 * Initializes the document holding the discussion context XClass.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named("Discussions.Code.DiscussionContextClass")
public class DiscussionContextXClassInitializer extends AbstractMandatoryClassInitializer
{
    private static final String STATIC_LISTS_SEPARATOR = ",";

    /**
     * Default constructor.
     */
    public DiscussionContextXClassInitializer()
    {
        super(DiscussionContextMetadata.XCLASS_REFERENCE);
    }

    @Override
    protected void createClass(BaseClass xClass)
    {
        int textSize = Integer.MAX_VALUE;
        xClass.addTextField(REFERENCE_NAME, REFERENCE_PRETTY_NAME, textSize);
        xClass.addTextField(ENTITY_REFERENCE_TYPE_NAME, ENTITY_REFERENCE_TYPE_PRETTY_NAME, textSize);
        xClass.addTextField(ENTITY_REFERENCE_NAME, ENTITY_REFERENCE_PRETTY_NAME, textSize);
        xClass.addTextField(NAME_NAME, NAME_PRETTY_NAME, textSize);
        xClass.addTextAreaField(DESCRIPTION_NAME, DESCRIPTION_PRETTY_NAME, 10, 10, WYSIWYG);
        xClass.addDateField(CREATION_DATE_NAME, CREATION_DATE_PRETTY_NAME);
        xClass.addDateField(UPDATE_DATE_NAME, UPDATE_DATE_PRETTY_NAME);
        xClass.addStaticListField(STATES_NAME, STATES_PRETTY_NAME, 1, true, true, "", DISPLAYTYPE_INPUT,
            STATIC_LISTS_SEPARATOR, "", FREE_TEXT_ALLOWED, false);
        xClass.addBooleanField(PINED_NAME, PINED_PRETTY_NAME);
        xClass.addStaticListField(DISCUSSIONS_NAME, DISCUSSIONS_PRETTY_NAME, 1, true, true, "", DISPLAYTYPE_INPUT,
            STATIC_LISTS_SEPARATOR, "", FREE_TEXT_ALLOWED, false);
    }
}
