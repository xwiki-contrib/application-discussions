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

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.RandomStringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.MessageStoreService;
import org.xwiki.contrib.discussions.store.meta.MessageMetadata;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseElement;
import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.DISCUSSION_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_NAME;

/**
 * Default implementation of {@link MessageStoreService}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultMessageStoreService implements MessageStoreService
{
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private DiscussionStoreService discussionStoreService;

    @Inject
    private MessageMetadata messageMetadata;

    @Override
    public Optional<String> create(String content, DocumentReference author, String discussionReference)
    {
        XWikiContext context = xcontextProvider.get();
        return this.discussionStoreService.get(discussionReference)
            .map(BaseElement::getDocumentReference)
            .flatMap(dr -> {
                try {
                    return Optional.of(context.getWiki().getDocument(dr, context));
                } catch (XWikiException e) {
                    e.printStackTrace();
                    return Optional.empty();
                }
            })
            .map(document -> {
                BaseObject object = new BaseObject();
                // get() is safe at this point.
                String messageReference = generateUniqueReference(discussionReference, document);
                object.setXClassReference(this.messageMetadata.getMessageXClass());
                object.set(REFERENCE_NAME, messageReference, context);
                object.set(AUTHOR_NAME, author, context);
                object.set(CONTENT_NAME, content, context);
                // TODO data
                // TODO replyTo
                object.set(DISCUSSION_REFERENCE_NAME, discussionReference, context);
                document.addXObject(object);
                return messageReference;
            });
    }

    private String generateUniqueReference(String discussionReference, XWikiDocument document)
    {
        String reference = generateReference(discussionReference);
        while (referenceExists(document, reference)) {
            reference = generateReference(discussionReference);
        }
        return reference;
    }

    private boolean referenceExists(XWikiDocument document, String reference)
    {
        return document.getXObjects().entrySet().stream().anyMatch(
            // Search for an existing message in the page with the same reference 
            entry -> Objects.equals(entry.getKey(), messageMetadata.getMessageXClass()) && entry.getValue().stream()
                .anyMatch(bo -> Objects.equals(bo.getStringValue(REFERENCE_NAME), reference)));
    }

    private String generateReference(String discussionReference)
    {
        int length = 6;
        boolean useLetters = true;
        boolean useNumbers = true;
        String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);
        return String.format("%s-%s", discussionReference, generatedString);
    }
}
