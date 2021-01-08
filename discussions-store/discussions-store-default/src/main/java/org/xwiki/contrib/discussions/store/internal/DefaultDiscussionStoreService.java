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
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.meta.DiscussionMetadata;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
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
    private DiscussionMetadata discussionMetadata;

    @Inject
    private QueryManager queryManager;

    @Inject
    private RandomGeneratorService randomGeneratorService;

    @Override
    public Optional<String> create(String title, String description)
    {
        Optional<String> reference;
        try {
            XWikiDocument document = generateUniquePage(title);
            XWikiContext context = this.getContext();
            BaseObject object = document.newXObject(this.discussionMetadata.getDiscussionXClass(), context);
            object.set(TITLE_NAME, title, context);
            object.set(DESCRIPTION_NAME, description, context);
            String pageName = document.getDocumentReference().getName();
            object.set(REFERENCE_NAME, pageName, context);
            Date value = new Date();
            object.setDateValue(UPDATE_DATE_NAME, value);
            object.setDateValue(CREATION_DATE_NAME, value);
            context.getWiki().saveDocument(document, context);
            reference = Optional.of(pageName);
        } catch (XWikiException e) {
            this.logger.warn("Failed to create a Discussion with title=[{}], description=[{}]. Cause: [{}]", title,
                description,
                getRootCauseMessage(e));
            reference = Optional.empty();
        }

        return reference;
    }

    @Override
    public Optional<BaseObject> get(String reference)
    {
        try {
            String discussionClass = this.discussionMetadata.getDiscussionXClassFullName();
            List<String> execute =
                this.queryManager
                    .createQuery(
                        String.format("FROM doc.object(%s) obj where obj.%s = :reference", discussionClass,
                            REFERENCE_NAME),
                        XWQL)
                    .bindValue("reference", reference)
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
        return Optional.of(document.getXObject(this.discussionMetadata.getDiscussionXClass()));
    }

    @Override
    public List<BaseObject> findByDiscussionContexts(List<String> discussionContextReferences)
    {
        String discussionClass = this.discussionMetadata.getDiscussionXClassFullName();
        try {
            // Selects the discussions that are linked EXACTLY to the requested discussion contexts.
            List<List<String>> subResults = new ArrayList<>();
            for (String discussionContextReference : discussionContextReferences) {
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
                    discussionClass), Query.HQL)
                    .bindValue("discussionContextReference", discussionContextReference)
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
                discussionClass), Query.HQL)
                .bindValue("contextsListSize", (long) discussionContextReferences.size()).execute());

            return intersection(subResults).stream()
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
    public void touch(String discussionReference)
    {
        get(discussionReference).ifPresent(discussion -> {
            discussion.setDateValue(UPDATE_DATE_NAME, new Date());
            save(discussion);
        });
    }

    @Override
    public boolean link(String discussionReference, String discussionContextReference)
    {
        return get(discussionReference)
            .map(discussion -> {
                List listValue = discussion.getListValue(DISCUSSION_CONTEXTS_NAME);
                if (!listValue.contains(discussionContextReference)) {
                    listValue.add(discussionContextReference);
                    save(discussion);
                    return true;
                }
                return false;
            }).orElse(false);
    }

    @Override
    public boolean unlink(String discussionReference, String discussionContextReference)
    {
        return get(discussionReference)
            .map(
                discussion -> {
                    List listValue = discussion.getListValue(DISCUSSION_CONTEXTS_NAME);
                    if (listValue.contains(discussionContextReference)) {
                        listValue.remove(discussionContextReference);
                        save(discussion);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }

    private XWikiDocument generateUniquePage(String title) throws XWikiException
    {
        XWikiDocument document;
        synchronized (this) {
            document = generatePage(title);

            while (!document.isNew()) {
                document = generatePage(title);
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

    private XWikiDocument generatePage(String title) throws XWikiException
    {
        String generatedString = this.randomGeneratorService.randomString();

        SpaceReference discussionContextSpace = this.discussionMetadata.getDiscussionSpace();
        DocumentReference documentReference =
            new DocumentReference(String.format("%s-%s", title, generatedString), discussionContextSpace);

        XWikiContext context = getContext();
        return context.getWiki().getDocument(documentReference, context);
    }

    private void save(BaseObject discussion)
    {
        XWikiContext context = getContext();
        try {
            discussion.setDateValue(UPDATE_DATE_NAME, new Date());
            context.getWiki().saveDocument(discussion.getOwnerDocument(), context);
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
