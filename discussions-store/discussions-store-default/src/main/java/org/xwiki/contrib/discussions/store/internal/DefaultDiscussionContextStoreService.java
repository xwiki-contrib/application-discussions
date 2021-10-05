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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.store.DiscussionContextStoreService;
import org.xwiki.contrib.discussions.store.DiscussionStoreConfiguration;
import org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata;
import org.xwiki.contrib.discussions.store.meta.DiscussionMetadata;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.CREATION_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.DISCUSSIONS_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.NAME_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.UPDATE_DATE_NAME;
import static org.xwiki.query.Query.XWQL;

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
    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private DiscussionContextMetadata discussionContextMetadata;

    @Inject
    private RandomGeneratorService randomGeneratorService;

    @Inject
    private QueryManager queryManager;

    @Inject
    private DiscussionStoreConfigurationFactory discussionStoreConfigurationFactory;

    @Inject
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    @Override
    public Optional<DiscussionContextReference> create(String applicationHint, String name, String description,
        DiscussionContextEntityReference entityReference)
    {
        Optional<DiscussionContextReference> result = Optional.empty();
        try {
            XWikiDocument document = generateUniquePage(applicationHint, name);
            XWikiContext context = this.getContext();
            EntityReference discussionContextXClass = this.discussionContextMetadata.getDiscussionContextXClass();
            BaseObject object = document.newXObject(discussionContextXClass, context);
            object.setXClassReference(this.discussionContextMetadata.getDiscussionContextXClass());

            object.set(NAME_NAME, name, context);
            object.set(DESCRIPTION_NAME, description, context);
            object.set(ENTITY_REFERENCE_TYPE_NAME, entityReference.getType(), context);
            object.set(ENTITY_REFERENCE_NAME, entityReference.getReference(), context);
            String pageName = document.getDocumentReference().getName();

            DiscussionContextReference reference = new DiscussionContextReference(applicationHint, pageName);
            String serializedReference = this.discussionReferencesSerializer.serialize(reference);
            object.set(REFERENCE_NAME, serializedReference, context);
            object.setDateValue(CREATION_DATE_NAME, new Date());
            object.setDateValue(UPDATE_DATE_NAME, new Date());
            document.setHidden(true);
            context.getWiki().saveDocument(document, context);

            result = Optional.of(reference);
        } catch (XWikiException e) {
            this.logger.warn(
                "Failed to create a Discussion Context with name=[{}], description=[{}],  entityReference=[{}]. "
                    + "Cause: [{}].", name, description, entityReference, getRootCauseMessage(e));
        }

        return result;
    }

    @Override
    public Optional<BaseObject> get(DiscussionContextReference reference)
    {
        try {
            String discussionClass = this.discussionContextMetadata.getDiscussionContextXClassFullName();
            List<String> execute =
                this.queryManager
                    .createQuery(
                        String.format("FROM doc.object(%s) obj where obj.%s = :%s",
                            discussionClass,
                            DiscussionMetadata.REFERENCE_NAME,
                            DiscussionMetadata.REFERENCE_NAME),
                        XWQL)
                    .bindValue(DiscussionMetadata.REFERENCE_NAME,
                        this.discussionReferencesSerializer.serialize(reference))
                    .execute();
            if (execute == null || execute.isEmpty()) {
                return Optional.empty();
            }
            if (execute.size() > 1) {
                this.logger.debug("More than one discussion found for reference=[{}]", reference);
            }
            String result = execute.get(0);

            return mapToBaseObject(result);
        } catch (QueryException | XWikiException e) {
            this.logger.warn("Failed to get the Discussion with reference=[{}]. Cause: [{}]", reference,
                getRootCauseMessage(e));
            return Optional.empty();
        }
    }

    private Optional<BaseObject> mapToBaseObject(String result) throws XWikiException
    {
        XWikiDocument document = this.xcontextProvider.get().getWiki()
            .getDocument(result, EntityType.DOCUMENT, this.xcontextProvider.get());
        return Optional.of(document.getXObject(this.discussionContextMetadata.getDiscussionContextXClass()));
    }

    @Override
    public boolean link(DiscussionContextReference discussionContextReference, DiscussionReference discussionReference)
    {
        return get(discussionContextReference)
            .map(discussionContext -> {
                List listValue = discussionContext.getListValue(DISCUSSIONS_NAME);
                String serializedReference = this.discussionReferencesSerializer.serialize(discussionReference);
                if (!listValue.contains(serializedReference)) {
                    listValue.add(serializedReference);
                    save(discussionContext);
                    return true;
                }
                return false;
            }).orElse(false);
    }

    @Override
    public boolean unlink(DiscussionContextReference discussionContextReference,
        DiscussionReference discussionReference)
    {
        return get(discussionContextReference)
            .map(discussionContext -> {
                List listValue = discussionContext.getListValue(DISCUSSIONS_NAME);
                String serializedReference = this.discussionReferencesSerializer.serialize(discussionReference);
                if (listValue.contains(serializedReference)) {
                    listValue.remove(serializedReference);
                    save(discussionContext);
                    return true;
                }
                return false;
            }).orElse(false);
    }

    @Override
    public Optional<BaseObject> findByReference(DiscussionContextEntityReference entityReference)
    {
        try {
            List<String> execute = this.queryManager.createQuery(String.format(
                "select doc.fullName "
                    + "from XWikiDocument as doc, "
                    + "BaseObject as obj, "
                    + "com.xpn.xwiki.objects.StringProperty as reference_field, "
                    + "com.xpn.xwiki.objects.StringProperty as type_field, "
                    + "com.xpn.xwiki.objects.StringProperty as entity_reference_field "
                    + "where doc.fullName=obj.name "
                    + "and obj.className='%s' "
                    + "and reference_field.id.name = '%s' "
                    + "and reference_field.id.id=obj.id "
                    + "and type_field.id.name = '%s' "
                    + "and type_field.id.id=obj.id "
                    + "and entity_reference_field.id.id=obj.id "
                    + "and entity_reference_field.id.name = '%s' "
                    + "and type_field.value = :type "
                    + "and entity_reference_field.value = :entityReference ",
                this.discussionContextMetadata.getDiscussionContextXClassFullName(), REFERENCE_NAME,
                ENTITY_REFERENCE_TYPE_NAME, ENTITY_REFERENCE_NAME),
                Query.HQL)
                .bindValue("type", entityReference.getType())
                .bindValue(ENTITY_REFERENCE_NAME, entityReference.getReference())
                .execute();

            if (execute == null || execute.isEmpty()) {
                return Optional.empty();
            }
            if (execute.size() > 1) {
                this.logger
                    .debug("More than one discussion context found for entityReference=[{}]", entityReference);
            }
            String result = execute.get(0);

            return mapToBaseObject(result);
        } catch (QueryException | XWikiException e) {
            this.logger.warn(
                "Failed to get the search a discussion context with  entityReference=[{}]. Cause: [{}]",
                entityReference, getRootCauseMessage(e));
        }
        return Optional.empty();
    }

    @Override
    public List<BaseObject> findByDiscussionReference(DiscussionReference reference)
    {
        try {
            String className =
                this.discussionContextMetadata.getDiscussionContextXClassFullName();
            return this.queryManager.createQuery(String.format("select doc.fullName "
                    + "from XWikiDocument as doc, "
                    + "BaseObject as obj, "
                    + "DBStringListProperty as discussions "
                    + "where doc.fullName=obj.name "
                    + "and obj.className='%s' "
                    + "and discussions.id.id=obj.id "
                    + "and discussions.name = '%s' "
                    + "and :reference in elements(discussions.list) ",
                className, DISCUSSIONS_NAME), Query.HQL)
                .bindValue("reference", this.discussionReferencesSerializer.serialize(reference))
                .<String>execute()
                .stream()
                .map(ref -> {
                    try {
                        return this.mapToBaseObject(ref);
                    } catch (XWikiException e) {
                        return Optional.<BaseObject>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        } catch (QueryException e) {
            this.logger.warn("Failed to query a discussion by reference [{}]. Cause: [{}].", reference,
                getRootCauseMessage(e));
            return Collections.emptyList();
        }
    }

    private XWikiDocument generateUniquePage(String applicationHint, String name) throws XWikiException
    {
        XWikiDocument document;
        synchronized (this) {
            document = generatePage(applicationHint, name);

            while (!document.isNew()) {
                document = generatePage(applicationHint, name);
            }
            XWikiContext context = getContext();
            document.setHidden(true);
            context.getWiki().saveDocument(document, context);
        }
        return document;
    }

    private XWikiContext getContext()
    {
        return this.xcontextProvider.get();
    }

    private XWikiDocument generatePage(String applicationHint, String name) throws XWikiException
    {
        String generatedString = this.randomGeneratorService.randomString();

        DiscussionStoreConfiguration discussionStoreConfiguration =
            this.discussionStoreConfigurationFactory.getDiscussionStoreConfiguration(applicationHint);
        SpaceReference discussionContextSpace = discussionStoreConfiguration.getDiscussionContextSpaceStorageLocation();
        DocumentReference documentReference =
            new DocumentReference(String.format("%s-%s", name, generatedString), discussionContextSpace);

        XWikiContext context = getContext();
        return context.getWiki().getDocument(documentReference, context);
    }

    private void save(BaseObject discussionContext)
    {
        XWikiContext context = this.getContext();
        try {
            context.getWiki().saveDocument(discussionContext.getOwnerDocument(), context);
        } catch (XWikiException e) {
            this.logger.warn("Failed to save the discussion context. Cause: [{}]", getRootCauseMessage(e));
        }
    }
}
