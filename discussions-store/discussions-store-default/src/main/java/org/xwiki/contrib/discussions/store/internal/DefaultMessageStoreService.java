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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.contrib.discussions.store.DiscussionStoreConfiguration;
import org.xwiki.contrib.discussions.store.MessageStoreService;
import org.xwiki.contrib.discussions.store.meta.MessageMetadata;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CREATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.DISCUSSION_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_NAME;
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
    private MessageMetadata messageMetadata;

    @Inject
    private QueryManager queryManager;

    @Inject
    private RandomGeneratorService randomGeneratorService;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private DiscussionStoreConfigurationFactory discussionStoreConfigurationFactory;

    @Inject
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    @Override
    public Optional<MessageReference> create(String content, Syntax syntax, String authorType,
        String authorReference, DiscussionReference discussionReference, String title)
    {
        XWikiContext context = this.xcontextProvider.get();
        Optional<MessageReference> result = Optional.empty();
        try {
            String applicationHint = discussionReference.getApplicationHint();
            XWikiDocument document = generateUniquePage(applicationHint);
            // The title of the page
            document.setTitle(title);
            document.setHidden(true);
            // FIXME: object class should come from initializers
            document.setContent("{{velocity}}\n"
                + "#set ($message = $doc.getObject('Discussions.Code.MessageClass'))\n"
                + "#set ($discussion = $services.discussions.getDiscussion($message.discussionReference))\n"
                + "$response.sendRedirect($xwiki.getURL($discussion.mainDocument, 'view', "
                + "\"reference=$message.discussionReference\"))\n"
                + "{{/velocity}}");
            DocumentReference authorReferenceDoc;
            if (authorType.equals("user")) {
                authorReferenceDoc = this.documentReferenceResolver.resolve(authorReference);
            } else {
                authorReferenceDoc = null;
            }
            document.setAuthorReference(authorReferenceDoc);
            BaseObject messageBaseObject = document.newXObject(this.messageMetadata.getMessageXClass(), context);
            String messageName = document.getDocumentReference().getName();
            MessageReference messageReference = new MessageReference(applicationHint, messageName);
            String serializedReference = this.discussionReferencesSerializer.serialize(messageReference);
            messageBaseObject.set(REFERENCE_NAME, serializedReference, context);
            messageBaseObject.set(AUTHOR_TYPE_NAME, authorType, context);
            messageBaseObject.set(AUTHOR_REFERENCE_NAME, authorReference, context);
            messageBaseObject.set(CONTENT_NAME, content, context);
            document.setSyntax(syntax);
            messageBaseObject.set(DISCUSSION_REFERENCE_NAME,
                this.discussionReferencesSerializer.serialize(discussionReference), context);
            Date now = new Date();
            messageBaseObject.setDateValue(CREATE_DATE_NAME, now);
            messageBaseObject.setDateValue(UPDATE_DATE_NAME, now);
            // TODO replyTo
            context.getWiki().saveDocument(document, context);

            result = Optional.of(messageReference);
        } catch (XWikiException e) {
            this.logger.warn(
                "Failed to create a Message with content=[{}], authorType=[{}], authorReference=[{}], "
                    + "discussionReference=[{}]. Cause: [{}].",
                content, authorReference, authorReference, discussionReference, getRootCauseMessage(e));
        }
        return result;
    }

    @Override
    public List<BaseObject> getByDiscussion(DiscussionReference discussionReference, int offset, int limit)
    {
        try {
            String messageClass = this.messageMetadata.getMessageXClassFullName();
            List<String> pageNames = this.queryManager.createQuery(String.format(
                " select doc.fullName "
                    + "from XWikiDocument as doc , "
                    + "BaseObject as obj , "
                    + "com.xpn.xwiki.objects.StringProperty as obj_discussionReference , "
                    + "com.xpn.xwiki.objects.DateProperty as obj_updateDate "
                    + "where obj_discussionReference.value = :discussionReference "
                    + "and doc.fullName=obj.name and obj.className='%s' "
                    + "and obj_discussionReference.id.id=obj.id "
                    + "and obj_updateDate.id.id=obj.id "
                    + "and obj_discussionReference.id.name='%s' "
                    + "and obj_updateDate.id.name='%s' "
                    + "order by obj_updateDate.value",
                messageClass, DISCUSSION_REFERENCE_NAME, UPDATE_DATE_NAME), Query.HQL)
                .setOffset(offset)
                .setLimit(limit)
                .bindValue("discussionReference", this.discussionReferencesSerializer.serialize(discussionReference))
                .execute();

            return getBaseObjects(pageNames);
        } catch (QueryException | XWikiException e) {
            this.logger.warn(
                "Failed to get the list Message for discussionReference=[{}], offset=[{}], limit=[{}]. Cause: [{}].",
                discussionReference, offset, limit, getRootCauseMessage(e));
            return emptyList();
        }
    }

    private List<BaseObject> getBaseObjects(List<String> pageNames)
        throws XWikiException
    {
        // FIXME: use a proper query
        XWikiContext context = this.xcontextProvider.get();
        return pageNames.stream().map(it -> {
            try {
                return Optional.ofNullable(context.getWiki().getDocument(it, EntityType.DOCUMENT,
                    context));
            } catch (XWikiException e) {
                return Optional.<XWikiDocument>empty();
            }
        }).filter(Optional::isPresent)
            .map(Optional::get)
            .map(it -> it.getXObject(this.messageMetadata.getMessageXClass()))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<BaseObject> getByReference(MessageReference reference)
    {
        try {
            String messageClass = this.messageMetadata.getMessageXClassFullName();
            List<String> pageNames = this.queryManager.createQuery(String.format(
                " select doc.fullName "
                    + "from XWikiDocument as doc , "
                    + "BaseObject as obj , "
                    + "com.xpn.xwiki.objects.StringProperty as obj_reference "
                    + "where obj_reference.value = :reference "
                    + "and doc.fullName=obj.name "
                    + "and obj.className='%s' "
                    + "and obj_reference.id.id=obj.id "
                    + "and obj_reference.id.name='%s' ",
                messageClass, REFERENCE_NAME), Query.HQL)
                .bindValue("reference", this.discussionReferencesSerializer.serialize(reference))
                .execute();

            return getBaseObjects(pageNames)
                .stream().findFirst();
        } catch (QueryException | XWikiException e) {
            this.logger.warn(
                "Failed to get the Message for reference=[{}]. Cause: [{}].",
                reference, getRootCauseMessage(e));
            return Optional.empty();
        }
    }

    @Override
    public Optional<BaseObject> getByEntityReference(EntityReference entityReference)
    {
        try {
            XWikiDocument document =
                this.xcontextProvider.get().getWiki().getDocument(entityReference, this.xcontextProvider.get());
            return Optional.of(document.getXObject(this.messageMetadata.getMessageXClass()));
        } catch (XWikiException e) {
            this.logger.warn(
                "Failed to get the Message for entityReference=[{}]. Cause: [{}].",
                entityReference, getRootCauseMessage(e));
            return Optional.empty();
        }
    }

    @Override
    public long countByDiscussion(DiscussionReference discussionReference)
    {
        String messageClass = this.messageMetadata.getMessageXClassFullName();
        long count;
        try {
            count = this.queryManager.createQuery(String.format(
                " select count(*) "
                    + "from XWikiDocument as doc , "
                    + "BaseObject as obj , "
                    + "com.xpn.xwiki.objects.StringProperty as discussionReferenceField "
                    + "where discussionReferenceField.value = :discussionReference "
                    + "and doc.fullName=obj.name "
                    + "and obj.className='%s' "
                    + "and discussionReferenceField.id.id=obj.id "
                    + "and discussionReferenceField.id.name='%s' ", messageClass, DISCUSSION_REFERENCE_NAME), Query.HQL)
                .bindValue("discussionReference", this.discussionReferencesSerializer.serialize(discussionReference))
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
    public void delete(MessageReference reference)
    {
        this.getByReference(reference)
            .ifPresent(message -> {
                try {
                    this.xcontextProvider.get().getWiki().deleteDocument(message.getOwnerDocument(),
                        this.xcontextProvider.get());
                } catch (XWikiException e) {
                    this.logger.warn("Failed to delete a Message with reference [{}]. Cause: [{}]", reference,
                        getRootCauseMessage(e));
                }
            });
    }

    private XWikiDocument generateUniquePage(String applicationHint) throws XWikiException
    {
        XWikiDocument document;
        synchronized (this) {
            document = generatePage(applicationHint);

            while (!document.isNew()) {
                document = generatePage(applicationHint);
            }
            document.setHidden(true);
            XWikiContext context = this.xcontextProvider.get();
            context.getWiki().saveDocument(document, context);
        }
        return document;
    }

    private XWikiDocument generatePage(String applicationHint) throws XWikiException
    {
        String generatedString = this.randomGeneratorService.randomString(10);
        DiscussionStoreConfiguration discussionStoreConfiguration =
            this.discussionStoreConfigurationFactory.getDiscussionStoreConfiguration(applicationHint);
        SpaceReference messageSpace = discussionStoreConfiguration.getMessageSpaceStorageLocation();
        DocumentReference documentReference = new DocumentReference(generatedString, messageSpace);

        XWikiContext context = this.xcontextProvider.get();
        return context.getWiki().getDocument(documentReference, context);
    }
}
