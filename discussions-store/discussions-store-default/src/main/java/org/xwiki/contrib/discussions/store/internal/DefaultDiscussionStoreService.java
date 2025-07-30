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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionException;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.meta.DiscussionMetadata;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.CREATION_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DISCUSSION_CONTEXTS_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.MAIN_DOCUMENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.TITLE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.UPDATE_DATE_NAME;
import static org.xwiki.query.Query.XWQL;

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
    private Logger logger;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private QueryManager queryManager;

    @Inject
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    @Inject
    private ContextualLocalizationManager localizationManager;

    @Inject
    private PageHolderReferenceFactory pageHolderReferenceFactory;

    @Inject
    private DocumentRedirectionManager documentRedirectionManager;

    @Inject
    private DocumentAuthorsManager documentAuthorsManager;

    @Override
    public BaseObject create(String applicationHint, String title, String description,
        String mainDocument, DiscussionStoreConfigurationParameters configurationParameters) throws
        DiscussionException
    {
        BaseObject result;
        try {
            XWikiDocument document = generateUniquePage(applicationHint, title, configurationParameters);
            XWikiContext context = this.getContext();
            BaseObject object = document.newXObject(DiscussionMetadata.XCLASS_REFERENCE, context);
            object.setStringValue(TITLE_NAME, title);
            object.setStringValue(DESCRIPTION_NAME, description);
            String pageName = document.getDocumentReference().getName();
            DiscussionReference reference = new DiscussionReference(applicationHint, pageName);
            String serializedReference = this.discussionReferencesSerializer.serialize(reference);
            object.setStringValue(REFERENCE_NAME, serializedReference);
            Date value = new Date();
            object.setDateValue(UPDATE_DATE_NAME, value);
            object.setDateValue(CREATION_DATE_NAME, value);
            object.setStringValue(MAIN_DOCUMENT_NAME, mainDocument);
            document.setHidden(true);
            documentAuthorsManager.setDocumentAuthors(document.getAuthors(), null, configurationParameters);
            this.documentRedirectionManager.handleCreatingRedirection(document, configurationParameters);
            context.getWiki().saveDocument(document, context);
            result = object;
        } catch (XWikiException e) {
            throw new DiscussionException(String.format("Failed to create a Discussion with title=[%s], "
                + "description=[%s].", title, description), e);
        }

        return result;
    }

    @Override
    public Optional<BaseObject> get(DiscussionReference reference)
    {
        try {
            List<String> execute =
                this.queryManager
                    .createQuery(
                        String.format("FROM doc.object(%s) obj where obj.%s = :reference",
                            DiscussionMetadata.XCLASS_FULLNAME, REFERENCE_NAME),
                        XWQL)
                    .bindValue("reference", this.discussionReferencesSerializer.serialize(reference))
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
            .getDocument(result, EntityType.DOCUMENT, this.xcontextProvider.get()).clone();
        return Optional.of(document.getXObject(DiscussionMetadata.XCLASS_REFERENCE));
    }

    @Override
    public List<BaseObject> findByDiscussionContexts(List<DiscussionContextReference> discussionContextReferences)
    {
        try {
            // Selects the discussions that are linked EXACTLY to the requested discussion contexts.
            List<List<String>> subResults = new ArrayList<>();
            for (DiscussionContextReference discussionContextReference : discussionContextReferences) {
                // Selects the discussions that are linked to the request contexts individually.
                subResults.add(this.queryManager.createQuery(String.format(
                    "select str_field.value "
                        + "from XWikiDocument as doc , "
                        + "BaseObject as obj , "
                        + "com.xpn.xwiki.objects.DBStringListProperty as field , "
                        + "com.xpn.xwiki.objects.StringProperty as str_field "
                        + "where (:discussionContextReference in elements(field.list)) "
                        + "and doc.fullName=obj.name "
                        + "and obj.className='%s' "
                        + "and field.id.id=obj.id "
                        + "and str_field.id.id=obj.id "
                        + "and field.id.name='discussionContexts' "
                        + "and str_field.id.name='reference' ",
                        DiscussionMetadata.XCLASS_FULLNAME), Query.HQL)
                    .bindValue("discussionContextReference",
                        this.discussionReferencesSerializer.serialize(discussionContextReference))
                    .execute());
            }
            // Selects the discussions that are linked to exactly as much contexts as the number requested.
            subResults.add(this.queryManager.createQuery(String.format(
                "select str_field.value "
                    + "from XWikiDocument as doc , "
                    + "BaseObject as obj , "
                    + "com.xpn.xwiki.objects.DBStringListProperty as field , "
                    + "com.xpn.xwiki.objects.StringProperty as str_field "
                    + "where doc.fullName=obj.name "
                    + "and obj.className='%s' "
                    + "and field.id.id=obj.id "
                    + "and str_field.id.id=obj.id "
                    + "and field.id.name='discussionContexts' "
                    + "and str_field.id.name='reference' "
                    + "GROUP BY str_field.value HAVING sum(size(field.list)) >= :contextsListSize",
                    DiscussionMetadata.XCLASS_FULLNAME), Query.HQL)
                .bindValue("contextsListSize", (long) discussionContextReferences.size()).execute());

            return intersection(subResults).stream()
                .map(result -> this.discussionReferencesResolver.resolve(result, DiscussionReference.class))
                .map(this::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        } catch (QueryException e) {
            this.logger.warn("Failed to retrieve the discussions link to the discussion contexts [{}]. Cause: [{}]",
                discussionContextReferences, getRootCauseMessage(e));
        }

        return emptyList();
    }

    @Override
    public List<BaseObject> findByEntityReferences(String type, List<String> references, Integer offset,
        Integer limit)
    {
        try {
            Query query = this.queryManager.createQuery("SELECT distinct doc.fullName, discussionUpdateDate.value "
                + "FROM XWikiDocument  doc, "
                + "XWikiDocument docDC, "
                + "BaseObject obj, "
                + "BaseObject objDC, "
                + "DBStringListProperty discussionContextReference, "
                + "DateProperty as discussionUpdateDate, "
                + "StringProperty discussionContextReferenceField, "
                + "StringProperty discussionReferenceField, "
                + "StringProperty discussionContextERType, "
                + "StringProperty discussionContextERRef "
                + "where doc.fullName = obj.name "
                + "AND docDC.fullName = objDC.name "
                + "AND obj.className='Discussions.Code.DiscussionClass' "
                + "AND objDC.className='Discussions.Code.DiscussionContextClass' "
                + "AND discussionContextReference.id.id = obj.id "
                + "AND discussionContextReference.name = 'discussionContexts' "
                + "AND discussionUpdateDate.id.id = obj.id "
                + "AND discussionUpdateDate.name = '" + UPDATE_DATE_NAME + "' "
                + "AND discussionReferenceField.id.id = obj.id "
                + "AND discussionReferenceField.name = 'reference' "
                + "AND discussionContextReferenceField.id.id = objDC.id "
                + "AND discussionContextReferenceField.name = 'reference' "
                + "AND discussionContextERType.id.id = objDC.id "
                + "AND discussionContextERType.name = 'entityReferenceType' "
                + "AND discussionContextERRef.id.id = objDC.id "
                + "AND discussionContextERRef.name = 'entityReference' "
                + "AND discussionContextReferenceField.value IN elements(discussionContextReference.list) "
                + "AND discussionContextERType.value = :type "
                + "AND discussionContextERRef.value IN :references "
                + "ORDER BY discussionUpdateDate.value DESC", Query.HQL)
                .bindValue("type", type)
                .bindValue("references", references);
            if (offset != null) {
                query = query.setOffset(offset);
            }
            if (limit != null) {
                query = query.setLimit(limit);
            }
            return query.<Object[]>execute()
                .stream()
                .map(it -> {
                    try {
                        return mapToBaseObject((String) it[0]);
                    } catch (XWikiException e) {
                        return Optional.<BaseObject>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        } catch (QueryException e) {
            this.logger.warn(
                "Failed to find an entity by reference with type [{}], reference [{}], offset [{}], and limit [{}]."
                    + " Cause: [{}].",
                type, references, offset, limit, getRootCauseMessage(e));
            return emptyList();
        }
    }

    @Override
    public long countByEntityReferences(String type, List<String> references)
    {
        long count;
        try {
            List<Long> execute = this.queryManager.createQuery("SELECT count(distinct discussionReferenceField.value) "
                + "FROM XWikiDocument  doc, "
                + "XWikiDocument docDC, "
                + "BaseObject obj, "
                + "BaseObject objDC, "
                + "DBStringListProperty discussionContextReference, "
                + "StringProperty discussionContextReferenceField, "
                + "StringProperty discussionReferenceField, "
                + "StringProperty discussionContextERType, "
                + "StringProperty discussionContextERRef "
                + "where doc.fullName = obj.name "
                + "AND docDC.fullName = objDC.name "
                + "AND obj.className='Discussions.Code.DiscussionClass' "
                + "AND objDC.className='Discussions.Code.DiscussionContextClass' "
                + "AND discussionContextReference.id.id = obj.id "
                + "AND discussionContextReference.name = 'discussionContexts' "
                + "AND discussionReferenceField.id.id = obj.id "
                + "AND discussionReferenceField.name = 'reference' "
                + "AND discussionContextReferenceField.id.id = objDC.id "
                + "AND discussionContextReferenceField.name = 'reference' "
                + "AND discussionContextERType.id.id = objDC.id "
                + "AND discussionContextERType.name = 'entityReferenceType' "
                + "AND discussionContextERRef.id.id = objDC.id "
                + "AND discussionContextERRef.name = 'entityReference' "
                + "AND discussionContextReferenceField.value IN elements(discussionContextReference.list) "
                + "AND discussionContextERType.value = :type "
                + "AND discussionContextERRef.value IN :references", Query.HQL)
                .bindValue("type", type)
                .bindValue("references", references).execute();
            count = execute.get(0);
        } catch (QueryException e) {
            this.logger
                .warn("Fail to count the discussions with type=[{}] and reference=[{}]. Cause: [{}].", type, references,
                    getRootCauseMessage(e));
            count = 0;
        }
        return count;
    }

    @Override
    public void touch(DiscussionReference discussionReference)
    {
        get(discussionReference).ifPresent(discussion -> {
            discussion.setDateValue(UPDATE_DATE_NAME, new Date());
            save(discussion, true, "discussions.store.discussion.updateDate");
        });
    }

    @Override
    public boolean link(DiscussionReference discussionReference, DiscussionContextReference discussionContextReference)
    {
        return get(discussionReference)
            .map(discussion -> {
                List listValue = new ArrayList(discussion.getListValue(DISCUSSION_CONTEXTS_NAME));
                String serializedReference = this.discussionReferencesSerializer.serialize(discussionContextReference);
                if (!listValue.contains(serializedReference)) {
                    listValue.add(serializedReference);
                    discussion.setDBStringListValue(DISCUSSION_CONTEXTS_NAME, listValue);
                    save(discussion, true, "discussions.store.discussion.linkContext");
                    return true;
                }
                return false;
            }).orElse(false);
    }

    @Override
    public boolean unlink(DiscussionReference discussionReference,
        DiscussionContextReference discussionContextReference)
    {
        return get(discussionReference)
            .map(
                discussion -> {
                    List listValue = new ArrayList(discussion.getListValue(DISCUSSION_CONTEXTS_NAME));
                    String serializedReference = this.discussionReferencesSerializer
                        .serialize(discussionContextReference);
                    if (listValue.contains(serializedReference)) {
                        listValue.remove(serializedReference);
                        discussion.setDBStringListValue(DISCUSSION_CONTEXTS_NAME, listValue);
                        save(discussion, true, "discussions.store.discussion.unlinkContext");
                        return true;
                    }
                    return false;
                }).orElse(false);
    }

    private XWikiDocument generateUniquePage(String applicationHint, String title,
        DiscussionStoreConfigurationParameters configurationParameters) throws XWikiException
    {
        XWikiDocument document;
        DocumentReference documentReference = this.pageHolderReferenceFactory.createPageHolderReference(
            PageHolderReferenceFactory.DiscussionEntity.DISCUSSION, title, applicationHint, null,
            configurationParameters);
        XWikiContext context = getContext();
        document = context.getWiki().getDocument(documentReference, context);
        document.setHidden(true);
        context.getWiki().saveDocument(document, context);
        return document;
    }

    private XWikiContext getContext()
    {
        return this.xcontextProvider.get();
    }

    private void save(BaseObject discussion, boolean minor, String translationKey)
    {
        XWikiContext context = getContext();
        try {
            discussion.setDateValue(UPDATE_DATE_NAME, new Date());
            context.getWiki().saveDocument(discussion.getOwnerDocument(),
                this.localizationManager.getTranslationPlain(translationKey), minor, context);
        } catch (XWikiException e) {
            this.logger.warn("Failed to save the discussion context. Cause: [{}]", getRootCauseMessage(e));
        }
    }

    /**
     * Returns the intersection of the provided lists.
     *
     * @param lists the lists
     * @return the element commons to all the provided lists
     */
    private List<String> intersection(List<List<String>> lists)
    {
        List<String> ret = lists.get(0);
        for (int i = 1; i < lists.size(); i++) {
            ret.retainAll(lists.get(i));
        }
        return ret;
    }
}
