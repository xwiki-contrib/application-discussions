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
package org.xwiki.contrib.discussions.store.internal;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.store.DiscussionContextMetadataStoreService;
import org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Default implementation of {@link DiscussionContextMetadataStoreService}.
 *
 * @version $Id$
 * @since 2.2
 */
@Component
@Singleton
public class DefaultDiscussionContextMetadataStoreService extends AbstractDiscussionContextStore
    implements DiscussionContextMetadataStoreService
{
    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private DiscussionContextMetadata discussionContextMetadata;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Override
    public boolean loadMetadata(DiscussionContext discussionContext)
    {
        return this.findDiscussionContextPage(discussionContext.getReference())
            .map(discussionContextPage -> {
                DocumentReference documentReference = this.documentReferenceResolver.resolve(discussionContextPage);
                return loadMetadata(documentReference, discussionContext);
            })
            .orElse(false);
    }

    @Override
    public boolean loadMetadata(DocumentReference discussionContextPage, DiscussionContext discussionContext)
    {
        XWikiContext context = this.xcontextProvider.get();
        boolean result = false;
        try {
            XWikiDocument document = context.getWiki().getDocument(discussionContextPage, context);
            result = this.loadMetadata(document, discussionContext);
        } catch (XWikiException e) {
            this.logger.error("Error while reading metadata objects from [{}]: [{}]", discussionContextPage,
                ExceptionUtils.getRootCauseMessage(e));
            this.logger.debug("Full stack trace for reading metadata error:", e);
        }
        return result;
    }

    @Override
    public boolean loadMetadata(XWikiDocument discussionContextDocument, DiscussionContext discussionContext)
    {
        boolean result = false;
        List<BaseObject> xObjects =
            discussionContextDocument.getXObjects(this.discussionContextMetadata.getDiscussionContextMetadataXClass());
        if (!xObjects.isEmpty()) {
            Map<String, String> metadataMap = discussionContext.getMetadata();
            metadataMap.clear();

            for (BaseObject xObject : xObjects) {
                metadataMap.put(xObject.getStringValue(DiscussionContextMetadata.METADATA_KEY),
                    xObject.getLargeStringValue(DiscussionContextMetadata.METADATA_VALUE));
            }
            result = true;
        }
        return result;
    }

    @Override
    public boolean saveMetadata(DiscussionContext discussionContext, Map<String, String> values)
    {
        Optional<String> discussionContextPage = this.findDiscussionContextPage(discussionContext.getReference());
        XWikiContext context = this.xcontextProvider.get();
        boolean result = false;
        if (discussionContextPage.isPresent()) {
            String serializedContextPage = discussionContextPage.get();
            DocumentReference documentReference = this.documentReferenceResolver.resolve(serializedContextPage);
            try {
                XWikiDocument document = context.getWiki().getDocument(documentReference, context);
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    this.updateMetadata(document, entry.getKey(), entry.getValue());
                }
                context.getWiki().saveDocument(document, "Add metadata", true, context);
                discussionContext.getMetadata().putAll(values);
                result = true;
            } catch (XWikiException e) {
                this.logger.error("Error while saving metadata value in page [{}]: [{}]", documentReference,
                    ExceptionUtils.getRootCauseMessage(e));
                this.logger.debug("Full stack trace:", e);
            }
        }
        return result;
    }

    private void updateMetadata(XWikiDocument document, String key, String value) throws XWikiException
    {
        XWikiContext context = this.xcontextProvider.get();
        DocumentReference discussionContextMetadataXClass =
            this.discussionContextMetadata.getDiscussionContextMetadataXClass();
        List<BaseObject> xObjects = document.getXObjects(discussionContextMetadataXClass);
        BaseObject metadataObject = null;
        for (BaseObject xObject : xObjects) {
            if (StringUtils.equals(xObject.getStringValue(DiscussionContextMetadata.METADATA_KEY), key)) {
                metadataObject = xObject;
                break;
            }
        }
        if (metadataObject == null) {
            int objectNumber = document.createXObject(discussionContextMetadataXClass, context);
            metadataObject = document.getXObject(discussionContextMetadataXClass, objectNumber);
            metadataObject.setStringValue(DiscussionContextMetadata.METADATA_KEY, key);
        }
        metadataObject.setLargeStringValue(DiscussionContextMetadata.METADATA_VALUE, value);
    }
}
