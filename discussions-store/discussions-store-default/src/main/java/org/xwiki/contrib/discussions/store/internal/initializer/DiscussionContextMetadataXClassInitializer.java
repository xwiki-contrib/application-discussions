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

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;

/**
 * Initializer of the xclass used to store discussion context metadata.
 *
 * @version $Id$
 * @since 1.5
 */
@Component
@Singleton
@Named("Discussions.Code.DiscussionContextMetadataClass")
public class DiscussionContextMetadataXClassInitializer extends AbstractDiscussionContextXClassInitializer
{
    @Inject
    private DiscussionContextMetadata discussionContextMetadata;

    @Override
    public EntityReference getDocumentReference()
    {
        return this.discussionContextMetadata.getDiscussionContextMetadataXClass();
    }

    @Override
    public boolean updateDocument(XWikiDocument document)
    {
        boolean needsUpdate = false;
        if (document.isNew()) {
            document.setHidden(true);
            BaseClass xClass = document.getXClass();
            xClass.addTextField(DiscussionContextMetadata.METADATA_KEY, DiscussionContextMetadata.METADATA_KEY, 100);
            xClass.addTextAreaField(DiscussionContextMetadata.METADATA_VALUE, DiscussionContextMetadata.METADATA_VALUE,
                25, 50, TextAreaClass.ContentType.PURE_TEXT);
            needsUpdate = true;
        } else {
            // Ensure that the content type is properly set
            // FIXME: we should change all that to use an AbstractMandatoryClassInitializer
            PropertyInterface metadataField = document.getXClass().get(DiscussionContextMetadata.METADATA_VALUE);
            if (metadataField != null) {
                TextAreaClass metadataFieldClass = (TextAreaClass) metadataField;
                if (!StringUtils.equalsIgnoreCase(metadataFieldClass.getContentType(),
                    TextAreaClass.ContentType.PURE_TEXT.toString())) {
                    metadataFieldClass.setContentType(TextAreaClass.ContentType.PURE_TEXT);
                }
                needsUpdate = true;
            }
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
