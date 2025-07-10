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

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.store.meta.DiscussionMetadata;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.EntityType;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DISCUSSION_CONTEXTS_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.UPDATE_DATE_NAME;

/**
 * Tests for {@link DefaultDiscussionStoreService}.
 *
 * @version $Id$
 * @since 2.0
 */
@ComponentTest
class DefaultDiscussionStoreServiceTest
{
    private static final String GET_QUERY =
        String.format("FROM doc.object(%s) obj where obj.reference = :reference", DiscussionMetadata.XCLASS_FULLNAME);
    private static final String DISCUSSION_CLASS = "DiscussionClass";

    @InjectMockComponents
    private DefaultDiscussionStoreService storeService;

    @MockComponent
    private Provider<XWikiContext> xcontextProvider;

    @MockComponent
    private QueryManager queryManager;

    @MockComponent
    private DiscussionStoreConfigurationFactory discussionStoreConfigurationFactory;

    @MockComponent
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    @MockComponent
    private DiscussionReferencesResolver discussionReferencesResolver;

    @MockComponent
    private ContextualLocalizationManager localizationManager;

    private XWikiContext context;
    private XWiki wiki;

    @BeforeEach
    void setup()
    {
        this.context = mock(XWikiContext.class);
        when(this.xcontextProvider.get()).thenReturn(this.context);
        this.wiki = mock(XWiki.class);
        when(this.context.getWiki()).thenReturn(this.wiki);
        when(this.localizationManager.getTranslationPlain(any()))
            .then(invocationOnMock -> invocationOnMock.getArgument(0));
    }

    @Test
    void get() throws Exception
    {
        DiscussionReference discussionReference = mock(DiscussionReference.class);
        String discussionPageRef = "Foo.Bar";
        Query query = mock(Query.class);
        when(this.queryManager.createQuery(GET_QUERY, Query.XWQL)).thenReturn(query);
        when(this.discussionReferencesSerializer.serialize(discussionReference)).thenReturn("d1");
        when(query.bindValue("reference", "d1")).thenReturn(query);
        when(query.execute()).thenReturn(Collections.singletonList(discussionPageRef));

        XWikiDocument document = mock(XWikiDocument.class);
        when(this.wiki.getDocument(discussionPageRef, EntityType.DOCUMENT, this.context)).thenReturn(document);

        BaseObject expectedObject = mock(BaseObject.class);
        when(document.getXObject(DiscussionMetadata.XCLASS_REFERENCE)).thenReturn(expectedObject);

        assertEquals(Optional.of(expectedObject), this.storeService.get(discussionReference));
        verify(query).bindValue("reference", "d1");
    }

    @Test
    void link() throws Exception
    {
        DiscussionReference discussionReference =
            new DiscussionReference("foo", "myDiscussionReference");
        DiscussionContextReference discussionContextReference =
            new DiscussionContextReference("foo", "myDiscussionContextReference");

        String serializedDiscussionReference = "foo:myDiscussionReference";
        String serializedDiscussionContextReference = "foo:myDiscussionContextReference";
        when(this.discussionReferencesSerializer.serialize(discussionReference))
            .thenReturn(serializedDiscussionReference);
        when(this.discussionReferencesSerializer.serialize(discussionContextReference))
            .thenReturn(serializedDiscussionContextReference);

        Query query1 = mock(Query.class);
        when(this.queryManager.createQuery(GET_QUERY, Query.XWQL)).thenReturn(query1);
        when(query1.bindValue("reference", serializedDiscussionReference)).thenReturn(query1);
        String docName = "objDoc1";
        when(query1.execute()).thenReturn(List.of(docName));
        XWikiDocument docObj = mock(XWikiDocument.class);
        when(this.wiki.getDocument(docName, EntityType.DOCUMENT, this.context)).thenReturn(docObj);
        BaseObject baseObject = mock(BaseObject.class);
        when(docObj.getXObject(DiscussionMetadata.XCLASS_REFERENCE)).thenReturn(baseObject);
        when(baseObject.getListValue(DISCUSSION_CONTEXTS_NAME)).thenReturn(List.of("ref1", "ref2"));
        when(baseObject.getOwnerDocument()).thenReturn(docObj);

        assertTrue(this.storeService.link(discussionReference, discussionContextReference));
        verify(query1).bindValue("reference", serializedDiscussionReference);
        verify(baseObject).setDBStringListValue(DISCUSSION_CONTEXTS_NAME,
            List.of("ref1", "ref2", serializedDiscussionContextReference));
        verify(baseObject).setDateValue(eq(UPDATE_DATE_NAME), any(Date.class));
        verify(this.wiki).saveDocument(docObj, "discussions.store.discussion.linkContext", true,this.context);
    }

    @Test
    void unlink() throws Exception
    {
        DiscussionReference discussionReference =
            new DiscussionReference("foo", "myDiscussionReference");
        DiscussionContextReference discussionContextReference =
            new DiscussionContextReference("foo", "myDiscussionContextReference");

        String serializedDiscussionReference = "foo:myDiscussionReference";
        String serializedDiscussionContextReference = "foo:myDiscussionContextReference";
        when(this.discussionReferencesSerializer.serialize(discussionReference))
            .thenReturn(serializedDiscussionReference);
        when(this.discussionReferencesSerializer.serialize(discussionContextReference))
            .thenReturn(serializedDiscussionContextReference);

        Query query1 = mock(Query.class);
        when(this.queryManager.createQuery(GET_QUERY, Query.XWQL)).thenReturn(query1);
        when(query1.bindValue("reference", serializedDiscussionReference)).thenReturn(query1);
        String docName = "objDoc1";
        when(query1.execute()).thenReturn(List.of(docName));
        XWikiDocument docObj = mock(XWikiDocument.class);
        when(this.wiki.getDocument(docName, EntityType.DOCUMENT, this.context)).thenReturn(docObj);
        BaseObject baseObject = mock(BaseObject.class);
        when(docObj.getXObject(DiscussionMetadata.XCLASS_REFERENCE)).thenReturn(baseObject);
        when(baseObject.getListValue(DISCUSSION_CONTEXTS_NAME)).thenReturn(List.of("ref1",
            serializedDiscussionContextReference, "ref2"));
        when(baseObject.getOwnerDocument()).thenReturn(docObj);

        assertTrue(this.storeService.unlink(discussionReference, discussionContextReference));
        verify(query1).bindValue("reference", serializedDiscussionReference);
        verify(baseObject).setDBStringListValue(DISCUSSION_CONTEXTS_NAME,
            List.of("ref1", "ref2"));
        verify(baseObject).setDateValue(eq(UPDATE_DATE_NAME), any(Date.class));
        verify(this.wiki).saveDocument(docObj, "discussions.store.discussion.unlinkContext", true,this.context);
    }

}
