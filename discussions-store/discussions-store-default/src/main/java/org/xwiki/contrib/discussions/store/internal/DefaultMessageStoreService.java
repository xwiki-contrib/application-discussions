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

import java.util.Comparator;
import java.util.Date;
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
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseElement;
import com.xpn.xwiki.objects.BaseObject;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CREATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.DISCUSSION_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.SYNTAX_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.UPDATE_DATE_NAME;

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
    public Optional<String> create(String content, Syntax syntax, String authorType, String authorReference,
        String discussionReference)
    {
        XWikiContext context = this.xcontextProvider.get();
        Optional<BaseObject> discussionBaseObject = this.discussionStoreService.get(discussionReference);
        if (!discussionBaseObject.isPresent()) {
            this.logger.warn("Discussion [{}] not found when creating a Message.", discussionReference);
        }
        return discussionBaseObject
            .map(BaseElement::getDocumentReference)
            .flatMap(dr -> {
                try {
                    return Optional.of(context.getWiki().getDocument(dr, context));
                } catch (XWikiException e) {
                    this.logger.warn("Failed to retrieve the document of the Discussion [{}]. Cause: [{}].", dr,
                        getRootCauseMessage(e));
                    return Optional.empty();
                }
            })
            .flatMap(document -> {
                Optional<String> ret;
                try {
                    BaseObject messageBaseObject =
                        document.newXObject(this.messageMetadata.getMessageXClass(), context);
                    String messageReference = generateUniqueReference(discussionReference, document);
                    messageBaseObject.set(REFERENCE_NAME, messageReference, context);
                    messageBaseObject.set(AUTHOR_TYPE_NAME, authorType, context);
                    messageBaseObject.set(AUTHOR_REFERENCE_NAME, authorReference, context);
                    messageBaseObject.set(CONTENT_NAME, content, context);
                    messageBaseObject.set(SYNTAX_NAME, syntax.toIdString(), context);
                    messageBaseObject.set(DISCUSSION_REFERENCE_NAME, discussionReference, context);
                    Date now = new Date();
                    messageBaseObject.setDateValue(CREATE_DATE_NAME, now);
                    messageBaseObject.setDateValue(UPDATE_DATE_NAME, now);
                    // TODO replyTo
                    context.getWiki().saveDocument(document, context);
                    ret = Optional.of(messageReference);
                } catch (XWikiException e) {
                    this.logger.warn(
                        "Failed to create a Message with content=[{}], authorType=[{}], authorReference=[{}], "
                            + "discussionReference=[{}]. Cause: [{}].",
                        content, authorReference, authorReference, discussionReference, getRootCauseMessage(e));
                    ret = Optional.empty();
                }
                return ret;
            });
    }

    @Override
    public List<BaseObject> getByDiscussion(String discussionReference, int offset, int limit)
    {
        try {
            String messageClass = this.messageMetadata.getMessageXClassFullName();
            List<String> messageReferences = this.queryManager.createQuery(String.format(
                " select obj_reference.value "
                    + "from XWikiDocument as doc , "
                    + "BaseObject as obj , "
                    + "com.xpn.xwiki.objects.StringProperty as obj_discussionReference , "
                    + "com.xpn.xwiki.objects.StringProperty as obj_reference, "
                    + "com.xpn.xwiki.objects.DateProperty as obj_updateDate "
                    + "where obj_discussionReference.value = :discussionReference "
                    + "and doc.fullName=obj.name and obj.className='%s' "
                    + "and obj_discussionReference.id.id=obj.id "
                    + "and obj_reference.id.id=obj.id "
                    + "and obj_updateDate.id.id=obj.id "
                    + "and obj_discussionReference.id.name='%s' "
                    + "and obj_reference.id.name='%s' "
                    + "and obj_updateDate.id.name='%s' "
                    + "order by obj_updateDate.value",
                messageClass, DISCUSSION_REFERENCE_NAME, REFERENCE_NAME, UPDATE_DATE_NAME), Query.HQL)
                .setOffset(offset)
                .setLimit(limit)
                .bindValue("discussionReference", discussionReference)
                .execute();

            return getBaseObjects(discussionReference, messageReferences);
        } catch (QueryException | XWikiException e) {
            this.logger.warn(
                "Failed to get the list Message for discussionReference=[{}], offset=[{}], limit=[{}]. Cause: [{}].",
                discussionReference, offset, limit, getRootCauseMessage(e));
            return emptyList();
        }
    }

    private List<BaseObject> getBaseObjects(String discussionReference, List<String> messageReferences)
        throws XWikiException
    {
        DocumentReference documentReference =
            this.discussionStoreService.get(discussionReference).get().getDocumentReference();
        XWikiContext context = this.xcontextProvider.get();
        XWikiDocument document = context.getWiki().getDocument(documentReference, context);

        return document.getXObjects(this.messageMetadata.getMessageXClass()).stream()
            .filter(Objects::nonNull)
            .filter(it -> messageReferences.contains(it.getStringValue(REFERENCE_NAME)))
            .sorted(Comparator.comparing(o -> o.getDateValue(UPDATE_DATE_NAME)))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<BaseObject> getByReference(String reference, String discussionReference)
    {
        Optional<BaseObject> baseObject;

        try {
            List<BaseObject> baseObjects = getBaseObjects(discussionReference, singletonList(reference));
            if (baseObjects.isEmpty()) {
                baseObject = Optional.empty();
            } else {

                if (baseObjects.size() > 1) {
                    this.logger
                        .debug("Found too many messages, expected one. With reference=[{}], discussionReference=[{}]",
                            reference, discussionReference);
                }

                baseObject = Optional.of(baseObjects.get(0));
            }
        } catch (XWikiException e) {
            this.logger.warn(
                "Failed to get a message by reference with reference=[{}], discussionReference=[{}]. Cause: [{}].",
                reference, discussionReference, getRootCauseMessage(e));
            baseObject = Optional.empty();
        }
        return baseObject;
    }

    @Override
    public long countByDiscussion(String discussionReference)
    {
        String messageClass = this.messageMetadata.getMessageXClassFullName();
        long count;
        try {
            count = this.queryManager.createQuery(String.format(
                " select count(*) "
                    + "from XWikiDocument as doc , "
                    + "BaseObject as obj , "
                    + "com.xpn.xwiki.objects.StringProperty as discussionReferenceField , "
                    + "com.xpn.xwiki.objects.StringProperty as messageReferenceField "
                    + "where discussionReferenceField.value = :discussionReference "
                    + "and doc.fullName=obj.name " 
                    + "and obj.className='%s' "
                    + "and discussionReferenceField.id.id=obj.id "
                    + "and messageReferenceField.id.id=obj.id "
                    + "and discussionReferenceField.id.name='%s' "
                    + "and messageReferenceField.id.name='%s'",
                messageClass, DISCUSSION_REFERENCE_NAME, REFERENCE_NAME), Query.HQL)
                .bindValue("discussionReference", discussionReference)
                .<Long>execute()
                .get(0);
        } catch (QueryException e) {
            this.logger
                .warn("Fail to count the messages with discussionReference=[{}]. Cause: [{}].", discussionReference,
                    getRootCauseMessage(e));
            count = 0;
        }
        return count;
    }

    @Override
    public void delete(String reference, String discussionReference)
    {
        this.getByReference(reference, discussionReference)
            .ifPresent(message -> {
                DocumentReference documentReference =
                    this.discussionStoreService.get(discussionReference).get().getDocumentReference();
                XWikiContext context = this.xcontextProvider.get();
                try {
                    XWikiDocument document = context.getWiki().getDocument(documentReference, context);
                    document.removeXObject(message);
                    context.getWiki().saveDocument(document, context);
                } catch (XWikiException e) {
                    this.logger
                        .warn("Failed to delete a message with reference=[{}], discussionReference=[{}]. Cause: [{}].",
                            reference, discussionReference, getRootCauseMessage(e));
                }
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
            entry -> Objects.equals(entry.getKey(), this.messageMetadata.getMessageXClass()) && entry.getValue()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(bo -> Objects.equals(bo.getStringValue(REFERENCE_NAME), reference)));
    }

    private String generateReference(String discussionReference)
    {
        String generatedString = this.randomGeneratorService.randomString();
        return String.format("%s-%s", discussionReference, generatedString);
    }
}
