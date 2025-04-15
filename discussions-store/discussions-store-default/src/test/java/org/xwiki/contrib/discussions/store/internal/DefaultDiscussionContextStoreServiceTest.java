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

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.DISCUSSIONS_NAME;

/**
 * Tests for {@link DefaultDiscussionContextStoreService}.
 *
 * @version $Id$
 * @since 2.5.3
 */
@ComponentTest
class DefaultDiscussionContextStoreServiceTest
{
    private static final String GET_QUERY =
        "FROM doc.object(DiscussionContextClass) obj where obj.reference = :reference";
    private static final String DISCUSSION_CONTEXT_CLASS = "DiscussionContextClass";

    @InjectMockComponents
    private DefaultDiscussionContextStoreService service;

    @MockComponent
    private Provider<XWikiContext> xcontextProvider;

    @MockComponent
    private ContextualLocalizationManager localizationManager;

    @MockComponent
    private PageHolderReferenceFactory pageHolderReferenceFactory;

    @MockComponent
    private DocumentRedirectionManager documentRedirectionManager;

    @MockComponent
    private DocumentAuthorsManager documentAuthorsManager;

    @MockComponent
    private DiscussionContextMetadata discussionContextMetadata;

    @MockComponent
    private QueryManager queryManager;

    @MockComponent
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    private XWikiContext context;
    private XWiki wiki;
    private DocumentReference discussionContextClass;

    @BeforeEach
    void setup()
    {
        this.context = mock(XWikiContext.class);
        when(this.xcontextProvider.get()).thenReturn(this.context);
        this.wiki = mock(XWiki.class);
        when(this.context.getWiki()).thenReturn(this.wiki);
        when(this.discussionContextMetadata.getDiscussionContextXClassFullName()).thenReturn(DISCUSSION_CONTEXT_CLASS);
        this.discussionContextClass = mock(DocumentReference.class);
        when(this.discussionContextMetadata.getDiscussionContextXClass()).thenReturn(discussionContextClass);
        when(this.localizationManager.getTranslationPlain(any()))
            .then(invocationOnMock -> invocationOnMock.getArgument(0));
    }

    @Test
    void get() throws QueryException, XWikiException
    {
        DiscussionContextReference discussionContextReference =
            new DiscussionContextReference("foo", "myDiscussionContextReference");
        String serializedDiscussionContextReference = "foo:myDiscussionContextReference";
        when(this.discussionReferencesSerializer.serialize(discussionContextReference))
            .thenReturn(serializedDiscussionContextReference);

        Query query1 = mock(Query.class);
        when(this.queryManager.createQuery(GET_QUERY, Query.XWQL)).thenReturn(query1);
        when(query1.bindValue("reference", serializedDiscussionContextReference)).thenReturn(query1);
        String docName = "objDoc1";
        when(query1.execute()).thenReturn(List.of(docName));
        XWikiDocument docObj = mock(XWikiDocument.class);
        when(this.wiki.getDocument(docName, EntityType.DOCUMENT, this.context)).thenReturn(docObj);
        BaseObject baseObject = mock(BaseObject.class);
        when(docObj.getXObject(this.discussionContextClass)).thenReturn(baseObject);
        assertEquals(Optional.of(baseObject), this.service.get(discussionContextReference));
        verify(query1).bindValue("reference", serializedDiscussionContextReference);
    }

    @Test
    void link() throws QueryException, XWikiException
    {
        DiscussionReference discussionReference =
            new DiscussionReference("foo", "myDiscussionReference");
        DiscussionContextReference discussionContextReference =
            new DiscussionContextReference("foo", "myDiscussionContextReference");
        String serializedDiscussionContextReference = "foo:myDiscussionContextReference";
        when(this.discussionReferencesSerializer.serialize(discussionContextReference))
            .thenReturn(serializedDiscussionContextReference);
        String serializedDiscussionReference = "foo:myDiscussionReference";
        when(this.discussionReferencesSerializer.serialize(discussionReference))
            .thenReturn(serializedDiscussionReference);

        Query query1 = mock(Query.class);
        when(this.queryManager.createQuery(GET_QUERY, Query.XWQL)).thenReturn(query1);
        when(query1.bindValue("reference", serializedDiscussionContextReference)).thenReturn(query1);
        String docName = "objDoc1";
        when(query1.execute()).thenReturn(List.of(docName));
        XWikiDocument docObj = mock(XWikiDocument.class);
        when(this.wiki.getDocument(docName, EntityType.DOCUMENT, this.context)).thenReturn(docObj);
        BaseObject baseObject = mock(BaseObject.class);
        when(docObj.getXObject(this.discussionContextClass)).thenReturn(baseObject);
        when(baseObject.getListValue(DISCUSSIONS_NAME)).thenReturn(List.of("ref1", "ref2"));
        when(baseObject.getOwnerDocument()).thenReturn(docObj);

        assertTrue(this.service.link(discussionContextReference, discussionReference));
        verify(query1).bindValue("reference", serializedDiscussionContextReference);
        verify(baseObject).setDBStringListValue(DISCUSSIONS_NAME,
            List.of("ref1", "ref2", serializedDiscussionReference));
        verify(this.wiki).saveDocument(docObj, "discussions.store.discussionContext.linkDiscussion", true,this.context);
    }

    @Test
    void unlink() throws QueryException, XWikiException
    {
        DiscussionReference discussionReference =
            new DiscussionReference("foo", "myDiscussionReference");
        DiscussionContextReference discussionContextReference =
            new DiscussionContextReference("foo", "myDiscussionContextReference");
        String serializedDiscussionContextReference = "foo:myDiscussionContextReference";
        when(this.discussionReferencesSerializer.serialize(discussionContextReference))
            .thenReturn(serializedDiscussionContextReference);
        String serializedDiscussionReference = "foo:myDiscussionReference";
        when(this.discussionReferencesSerializer.serialize(discussionReference))
            .thenReturn(serializedDiscussionReference);

        Query query1 = mock(Query.class);
        when(this.queryManager.createQuery(GET_QUERY, Query.XWQL)).thenReturn(query1);
        when(query1.bindValue("reference", serializedDiscussionContextReference)).thenReturn(query1);
        String docName = "objDoc1";
        when(query1.execute()).thenReturn(List.of(docName));
        XWikiDocument docObj = mock(XWikiDocument.class);
        when(this.wiki.getDocument(docName, EntityType.DOCUMENT, this.context)).thenReturn(docObj);
        BaseObject baseObject = mock(BaseObject.class);
        when(docObj.getXObject(this.discussionContextClass)).thenReturn(baseObject);
        when(baseObject.getListValue(DISCUSSIONS_NAME)).thenReturn(
            List.of("ref1", serializedDiscussionReference, "ref2"));
        when(baseObject.getOwnerDocument()).thenReturn(docObj);

        assertTrue(this.service.unlink(discussionContextReference, discussionReference));
        verify(query1).bindValue("reference", serializedDiscussionContextReference);
        verify(baseObject).setDBStringListValue(DISCUSSIONS_NAME, List.of("ref1", "ref2"));
        verify(this.wiki).saveDocument(docObj, "discussions.store.discussionContext.unlinkDiscussion", true,
            this.context);
    }
}