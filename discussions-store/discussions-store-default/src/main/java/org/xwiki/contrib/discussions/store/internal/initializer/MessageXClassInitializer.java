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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.meta.MessageMetadata;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;

import static com.xpn.xwiki.objects.classes.ListClass.DISPLAYTYPE_INPUT;
import static com.xpn.xwiki.objects.classes.ListClass.FREE_TEXT_ALLOWED;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CREATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CREATE_DATE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.DISCUSSION_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.DISCUSSION_REFERENCE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.PINED_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.PINED_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REPLY_TO_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REPLY_TO_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.STATES_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.STATES_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.UPDATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.UPDATE_DATE_PRETTY_NAME;

/**
 * Initializes the document holding the message XClass.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named("Discussions.Code.MessageClass")
public class MessageXClassInitializer extends AbstractDiscussionContextXClassInitializer
{
    private static final String STATIC_LISTS_SEPARATOR = ",";

    @Inject
    private MessageMetadata messageMetadata;

    @Override
    public EntityReference getDocumentReference()
    {
        return this.messageMetadata.getMessageXClass();
    }

    @Override
    public boolean updateDocument(XWikiDocument document)
    {
        boolean needsUpdate = false;
        if (document.isNew()) {
            document.setHidden(true);
            BaseClass xClass = document.getXClass();
            int textSize = Integer.MAX_VALUE;
            xClass.addTextField(REFERENCE_NAME, REFERENCE_PRETTY_NAME, textSize);
            xClass.addTextAreaField(CONTENT_NAME, CONTENT_PRETTY_NAME, 10, 10, TextAreaClass.EditorType.WYSIWYG);
            xClass.addTextField(DISCUSSION_REFERENCE_NAME, DISCUSSION_REFERENCE_PRETTY_NAME, textSize);
            xClass.addTextField(AUTHOR_TYPE_NAME, AUTHOR_TYPE_PRETTY_NAME, textSize);
            xClass.addTextField(AUTHOR_REFERENCE_NAME, AUTHOR_REFERENCE_PRETTY_NAME, textSize);
            xClass.addDateField(UPDATE_DATE_NAME, UPDATE_DATE_PRETTY_NAME);
            xClass.addDateField(CREATE_DATE_NAME, CREATE_DATE_PRETTY_NAME);
            xClass.addStaticListField(STATES_NAME, STATES_PRETTY_NAME, 1, true, true, "", DISPLAYTYPE_INPUT,
                STATIC_LISTS_SEPARATOR, "", FREE_TEXT_ALLOWED, false);
            xClass.addTextField(REPLY_TO_NAME, REPLY_TO_PRETTY_NAME, textSize);
            xClass.addBooleanField(PINED_NAME, PINED_PRETTY_NAME);
            needsUpdate = true;
        }

        if (initAuthorReference(document)) {
            needsUpdate = true;
        }

        if (initCreatorReference(document)) {
            needsUpdate = true;
        }

        return needsUpdate;
    }
}
