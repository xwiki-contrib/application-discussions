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

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.meta.DiscussionMetadata;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.TITLE_NAME;
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
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private DiscussionMetadata discussionMetadata;

    @Inject
    private QueryManager queryManager;

    @Inject
    private RandomGeneratorService randomGeneratorService;

    @Override
    public String create(String title, String description)
    {
        String reference = null;
        try {
            XWikiDocument document = generateUniquePage(title);
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
                // TODO: log incoherent data
            }
            String result = execute.get(0);

            XWikiDocument document = this.xcontextProvider.get().getWiki()
                .getDocument(result, EntityType.DOCUMENT, this.xcontextProvider.get());
            return Optional.of(document.getXObject(this.discussionMetadata.getDiscussionXClass()));
        } catch (QueryException | XWikiException e) {
            e.printStackTrace();
            // TODO log
            return Optional.empty();
        }
    }

    private XWikiDocument generateUniquePage(String title) throws XWikiException
    {
        // TODO: Check if ok regarding concurrency.
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
}
