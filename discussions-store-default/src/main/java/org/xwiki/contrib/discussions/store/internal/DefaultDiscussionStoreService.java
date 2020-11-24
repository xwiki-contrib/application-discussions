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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.RandomStringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.internal.meta.DiscussionMetadata;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.store.internal.meta.DiscussionMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.internal.meta.DiscussionMetadata.TITLE_NAME;
import static org.xwiki.contrib.discussions.store.internal.meta.DiscussionMetadata.REFERENCE_NAME;

/**
 * Default implementation of {@link DiscussionStoreService}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultDiscussionStoreService implements DiscussionStoreService
{
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private DiscussionMetadata discussionMetadata;

    @Override
    public String createDiscussion(String title, String description)
    {
        String reference = null;
        try {
            XWikiDocument document = generateUniqueDiscussionPage(title);
            BaseObject object = new BaseObject();
            object.setXClassReference(this.discussionMetadata.getDiscussionXClass());
            XWikiContext context = this.getContext();
            object.set(TITLE_NAME, title, context);
            object.set(DESCRIPTION_NAME, description, context);
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

    private XWikiDocument generateUniqueDiscussionPage(String title) throws XWikiException
    {
        // TODO: Check if how regarding concurrency.
        XWikiDocument document;
        synchronized (this) {
            document = generateDiscussionPage(title);

            while (!document.isNew()) {
                document = generateDiscussionPage(title);
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

    private XWikiDocument generateDiscussionPage(String title) throws XWikiException
    {
        int length = 6;
        boolean useLetters = true;
        boolean useNumbers = true;
        String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);

        SpaceReference discussionContextSpace = this.discussionMetadata.getDiscussionSpace();
        DocumentReference documentReference =
            new DocumentReference(String.format("%s-%s", title, generatedString), discussionContextSpace);

        XWikiContext context = getContext();
        return context.getWiki().getDocument(documentReference, context);
    }
}
