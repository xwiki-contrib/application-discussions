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
import org.xwiki.contrib.discussions.store.meta.DiscussionMetadata;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;

import static com.xpn.xwiki.objects.classes.ListClass.DISPLAYTYPE_INPUT;
import static com.xpn.xwiki.objects.classes.ListClass.FREE_TEXT_ALLOWED;
import static com.xpn.xwiki.objects.classes.TextAreaClass.EditorType.WYSIWYG;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.CREATION_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.CREATION_DATE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DESCRIPTION_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DISCUSSION_CONTEXTS_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DISCUSSION_CONTEXTS_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.MAIN_DOCUMENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.MAIN_DOCUMENT_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.PINED_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.PINED_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.REFERENCE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.STATES_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.STATES_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.TITLE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.TITLE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.UPDATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.UPDATE_DATE_PRETTY_NAME;

/**
 * Initializes the document holding the discussion XClass.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named("Discussions.Code.DiscussionClass")
public class DiscussionXClassInitializer extends AbstractMandatoryClassInitializer
{
    private static final String STATIC_LISTS_SEPARATOR = ",";

    /**
     * Default constructor.
     */
    public DiscussionXClassInitializer()
    {
        super(DiscussionMetadata.XCLASS_REFERENCE);
    }

    @Override
    protected void createClass(BaseClass xClass)
    {
        int textSize = Integer.MAX_VALUE;
        xClass.addTextField(REFERENCE_NAME, REFERENCE_PRETTY_NAME, textSize);
        xClass.addTextField(TITLE_NAME, TITLE_PRETTY_NAME, textSize);
        xClass.addTextAreaField(DESCRIPTION_NAME, DESCRIPTION_PRETTY_NAME, 10, 10, WYSIWYG);
        xClass.addDateField(CREATION_DATE_NAME, CREATION_DATE_PRETTY_NAME);
        xClass.addDateField(UPDATE_DATE_NAME, UPDATE_DATE_PRETTY_NAME);
        xClass.addStaticListField(STATES_NAME, STATES_PRETTY_NAME, 1, true, true, "", DISPLAYTYPE_INPUT,
            STATIC_LISTS_SEPARATOR, "", FREE_TEXT_ALLOWED, false);
        xClass.addBooleanField(PINED_NAME, PINED_PRETTY_NAME);
        xClass.addStaticListField(DISCUSSION_CONTEXTS_NAME, DISCUSSION_CONTEXTS_PRETTY_NAME, 1, true, true, "",
            DISPLAYTYPE_INPUT, STATIC_LISTS_SEPARATOR, "", FREE_TEXT_ALLOWED, false);
        xClass.addTextField(MAIN_DOCUMENT_NAME, MAIN_DOCUMENT_PRETTY_NAME, textSize);
    }
}
