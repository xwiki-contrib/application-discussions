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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.RandomStringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.DiscussionContextStoreService;
import org.xwiki.contrib.discussions.store.internal.meta.DiscussionContextMetadata;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.store.internal.meta.DiscussionContextMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.internal.meta.DiscussionContextMetadata.DISCUSSIONS_NAME;
import static org.xwiki.contrib.discussions.store.internal.meta.DiscussionContextMetadata.ENTITY_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.internal.meta.DiscussionContextMetadata.ENTITY_REFERENCE_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.internal.meta.DiscussionContextMetadata.NAME_NAME;
import static org.xwiki.contrib.discussions.store.internal.meta.DiscussionContextMetadata.REFERENCE_NAME;

/**
 * Default implementation of {@link DiscussionContextStoreService}.
 *
 * @version $Id$
 * @since 1.0s
 */
@Component
@Singleton
public class DefaultDiscussionContextStoreService implements DiscussionContextStoreService
{
    private static final List<String> DISCUSSION_CONTEXT_SPACE = Arrays.asList("Discussions", "DiscussionContext");

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private DiscussionContextMetadata discussionContextMetadata;

    @Override
    public String createDiscussionContext(String name, String description, String referenceType,
        String entityReference)
    {
        String reference = null;
        try {
            XWikiDocument document = generateUniqueDiscussionContextPage(name);
            BaseObject object = new BaseObject();
            object.setXClassReference(this.discussionContextMetadata.getDiscussionContextXClass());
            XWikiContext context = this.getContext();
            object.set(NAME_NAME, name, context);
            object.set(DESCRIPTION_NAME, description, context);
            object.set(ENTITY_REFERENCE_TYPE_NAME, referenceType, context);
            object.set(ENTITY_REFERENCE_NAME, entityReference, context);
            reference = document.getDocumentReference().getName();
            object.set(REFERENCE_NAME, reference, context);
            document.addXObject(object);
            context.getWiki().saveDocument(document, context);
        } catch (XWikiException e) {
            e.printStackTrace();
            // TODO: log, wrap and rethrow
        }

        return reference;
    }

    private XWikiDocument generateUniqueDiscussionContextPage(String name) throws XWikiException
    {
        // TODO: Check if how regarding concurrency.
        XWikiDocument document;
        synchronized (this) {
            document = generateDiscussionContextPage(name);

            while (!document.isNew()) {
                document = generateDiscussionContextPage(name);
            }
            XWikiContext context = getContext();
            context.getWiki().saveDocument(document, context);
        }
        return document;
    }

    private XWikiContext getContext()
    {
        return this.xcontextProvider.get();
    }

    private XWikiDocument generateDiscussionContextPage(String name) throws XWikiException
    {
        int length = 6;
        boolean useLetters = true;
        boolean useNumbers = true;
        String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);

        XWikiContext context = getContext();
        String currentWikiName = context.getMainXWiki();

        DocumentReference documentReference = new DocumentReference(currentWikiName, DISCUSSION_CONTEXT_SPACE,
            String.format("%s-%s", name, generatedString));
        return context.getWiki().getDocument(documentReference, context);
    }
}
