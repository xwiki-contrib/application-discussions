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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.MessageStoreService;
import org.xwiki.contrib.discussions.store.meta.MessageMetadata;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseElement;
import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
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
    private Logger logger;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private DiscussionStoreService discussionStoreService;

    @Inject
    private MessageMetadata messageMetadata;

    @Inject
    private QueryManager queryManager;

    @Inject
    private RandomGeneratorService randomGeneratorService;

    @Override
    public Optional<String> create(String content, String authorType, String authorReference,
        String discussionReference)
    {
        XWikiContext context = xcontextProvider.get();
        Optional<BaseObject> baseObject = this.discussionStoreService.get(discussionReference);
        if (!baseObject.isPresent()) {
            this.logger.warn("Discussion [{}] not found when creating a Message.", discussionReference);
        }
        return baseObject
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
                object.set(AUTHOR_TYPE_NAME, authorType, context);
                object.set(AUTHOR_REFERENCE_NAME, authorReference, context);
                object.set(CONTENT_NAME, content, context);
                // TODO data
                // TODO replyTo
                object.set(DISCUSSION_REFERENCE_NAME, discussionReference, context);
                document.addXObject(object);
                try {
                    context.getWiki().saveDocument(document, context);
                } catch (XWikiException e) {
                    e.printStackTrace();
                    // TODO log and react
                }
                return messageReference;
            });
    }

    @Override
    public List<BaseObject> getByDiscussion(String discussionReference, int offset, int limit)
    {

        try {
            String messageClass = this.messageMetadata.getMessageXClassFullName();
            List<String> discussionReference1 = this.queryManager.createQuery(String.format(
                " select obj_discussionReference2.value "
                    + "from XWikiDocument as doc , "
                    + "BaseObject as obj , "
                    + "com.xpn.xwiki.objects.StringProperty as obj_discussionReference1 , "
                    + "com.xpn.xwiki.objects.StringProperty as obj_discussionReference2 "
                    + "where ( obj_discussionReference1.value = :discussionReference ) "
                    + "and doc.fullName=obj.name and obj.className='%s' "
                    + "and obj_discussionReference1.id.id=obj.id "
                    + "and obj_discussionReference2.id.id=obj.id "
                    + "and obj_discussionReference1.id.name='%s' "
                    + "and obj_discussionReference2.id.name='%s'",
                messageClass, DISCUSSION_REFERENCE_NAME, REFERENCE_NAME), Query.HQL)
                .setOffset(offset)
                .setLimit(limit)
                .bindValue("discussionReference", discussionReference)
                .execute();

            DocumentReference documentReference =
                this.discussionStoreService.get(discussionReference).get().getDocumentReference();
            XWikiContext context = this.xcontextProvider.get();
            XWikiDocument document = context.getWiki().getDocument(documentReference, context);

            return document.getXObjects(this.messageMetadata.getMessageXClass()).stream()
                .filter(it -> discussionReference1.contains(it.getStringValue("reference")))
                .collect(Collectors.toList());
        } catch (QueryException | XWikiException e) {
            e.printStackTrace();
            // TODO log
            return null;
        }
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
            entry -> Objects.equals(entry.getKey(), this.messageMetadata.getMessageXClass()) && entry.getValue()
                .stream()
                .anyMatch(bo -> Objects.equals(bo.getStringValue(REFERENCE_NAME), reference)));
    }

    private String generateReference(String discussionReference)
    {
        String generatedString = this.randomGeneratorService.randomString();
        return String.format("%s-%s", discussionReference, generatedString);
    }
}
