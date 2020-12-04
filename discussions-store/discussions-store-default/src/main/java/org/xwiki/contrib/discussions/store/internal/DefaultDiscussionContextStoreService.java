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
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.DiscussionContextStoreService;
import org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata;
import org.xwiki.contrib.discussions.store.meta.DiscussionMetadata;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.NAME_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.REFERENCE_NAME;
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

    @Override
    public Optional<String> create(String name, String description, String referenceType,
        String entityReference)
    {
        Optional<String> reference;
        try {
            XWikiDocument document = generateUniquePage(name);
            XWikiContext context = this.getContext();
            EntityReference discussionContextXClass = this.discussionContextMetadata.getDiscussionContextXClass();
            BaseObject object = document.newXObject(discussionContextXClass, context);
            object.setXClassReference(this.discussionContextMetadata.getDiscussionContextXClass());

            object.set(NAME_NAME, name, context);
            object.set(DESCRIPTION_NAME, description, context);
            object.set(ENTITY_REFERENCE_TYPE_NAME, referenceType, context);
            object.set(ENTITY_REFERENCE_NAME, entityReference, context);
            String pageName = document.getDocumentReference().getName();
            object.set(REFERENCE_NAME, pageName, context);
            context.getWiki().saveDocument(document, context);
            reference = Optional.of(pageName);
        } catch (XWikiException e) {
            this.logger.warn(
                "Failed to create a Discussion Context with name=[{}], description=[{}], referenceType=[{}], "
                    + "entityReference=[{}]. Cause: [{}].",
                name, description, referenceType, entityReference, getRootCauseMessage(e));
            reference = Optional.empty();
        }

        return reference;
    }

    @Override
    public Optional<BaseObject> get(String reference)
    {
        try {
            String discussionClass = this.discussionContextMetadata.getDiscussionContextXClassFullName();
            List<String> execute =
                this.queryManager
                    .createQuery(
                        String.format("FROM doc.object(%s) obj where obj.%s = :reference", discussionClass,
                            DiscussionMetadata.REFERENCE_NAME),
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
        return Optional.of(document.getXObject(this.discussionContextMetadata.getDiscussionContextXClass()));
    }

    @Override
    public void link(String discussionContextReference, String discussionReference)
    {
        get(discussionContextReference)
            .ifPresent(discussionContext -> {
                List listValue = discussionContext.getListValue(DiscussionContextMetadata.DISCUSSIONS_NAME);
                if (!listValue.contains(discussionReference)) {
                    listValue.add(discussionReference);
                    save(discussionContext);
                }
            });
    }

    @Override
    public void unlink(String discussionContextReference, String discussionReference)
    {
        get(discussionContextReference)
            .ifPresent(discussionContext -> {
                discussionContext.getListValue(DiscussionContextMetadata.DISCUSSIONS_NAME)
                    .remove(discussionReference);
                save(discussionContext);
            });
    }

    private XWikiDocument generateUniquePage(String name) throws XWikiException
    {
        // TODO: Check if how regarding concurrency.
        XWikiDocument document;
        synchronized (this) {
            document = generatePage(name);

            while (!document.isNew()) {
                document = generatePage(name);
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

    private XWikiDocument generatePage(String name) throws XWikiException
    {
        String generatedString = this.randomGeneratorService.randomString();

        SpaceReference discussionContextSpace = this.discussionContextMetadata.getDiscussionContextSpace();
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
