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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.meta.MessageMetadata;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import ch.qos.logback.classic.Level;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.DISCUSSION_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_NAME;
import static org.xwiki.rendering.syntax.Syntax.XWIKI_2_1;
import static org.xwiki.test.LogLevel.DEBUG;

/**
 * Test of {@link DefaultMessageStoreService}.
 */
@ComponentTest
class DefaultMessageStoreServiceTest
{
    @InjectMockComponents
    private DefaultMessageStoreService defaultMessageStoreService;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(DEBUG);

    @MockComponent
    private Provider<XWikiContext> xcontextProvider;

    @MockComponent
    private DiscussionStoreService discussionStoreService;

    @MockComponent
    private MessageMetadata messageMetadata;

    @MockComponent
    private QueryManager queryManager;

    @MockComponent
    private RandomGeneratorService randomGeneratorService;

    @Mock
    private XWikiContext xWikiContext;

    @Mock
    private XWiki xWiki;

    @BeforeEach
    void setUp() throws Exception
    {
        when(this.xcontextProvider.get()).thenReturn(this.xWikiContext);
        when(this.xWikiContext.getWiki()).thenReturn(this.xWiki);
    }

    @Test
    void createWrongDiscussion()
    {
        when(this.discussionStoreService.get("discussionReference")).thenReturn(Optional.empty());
        Optional<String> reference =
            this.defaultMessageStoreService.create("content", XWIKI_2_1, "authorType", "authorReference",
                "discussionReference");
        assertFalse(reference.isPresent());
        assertEquals(1, this.logCapture.size());
        assertEquals(Level.WARN, this.logCapture.getLogEvent(0).getLevel());
        assertEquals("Discussion [discussionReference] not found when creating a Message.",
            this.logCapture.getMessage(0));
    }

    @Test
    void create() throws Exception
    {
        BaseObject discussionBaseObject = mock(BaseObject.class);
        BaseObject messageBaseObject = mock(BaseObject.class);
        DocumentReference discussionDocumentReference = new DocumentReference("xwiki", "XWiki", "DiscussionObject");
        XWikiDocument xWikiDocument = mock(XWikiDocument.class);
        DocumentReference messageXClassDocumentReference = new DocumentReference("xwiki", "XWiki", "MessageClass");

        when(discussionBaseObject.getDocumentReference()).thenReturn(discussionDocumentReference);
        when(this.discussionStoreService.get("discussionReference")).thenReturn(Optional.of(discussionBaseObject));
        when(this.xWiki.getDocument(discussionDocumentReference, this.xWikiContext)).thenReturn(xWikiDocument);
        when(xWikiDocument.newXObject(messageXClassDocumentReference, this.xWikiContext)).thenReturn(messageBaseObject);
        when(this.randomGeneratorService.randomString()).thenReturn("randomString", "randomString2");
        when(this.messageMetadata.getMessageXClass()).thenReturn(messageXClassDocumentReference);

        Optional<String> reference =
            this.defaultMessageStoreService.create("content", XWIKI_2_1, "authorType", "authorReference",
                "discussionReference");
        assertEquals(Optional.of("discussionReference-randomString"), reference);
        assertEquals(0, this.logCapture.size());
        verify(messageBaseObject).set(REFERENCE_NAME, "discussionReference-randomString", this.xWikiContext);
        verify(messageBaseObject).set(AUTHOR_TYPE_NAME, "authorType", this.xWikiContext);
        verify(messageBaseObject).set(AUTHOR_REFERENCE_NAME, "authorReference", this.xWikiContext);
        verify(messageBaseObject).set(CONTENT_NAME, "content", this.xWikiContext);
        verify(messageBaseObject).set(DISCUSSION_REFERENCE_NAME, "discussionReference", this.xWikiContext);
    }

    @Test
    void getByDiscussion() throws Exception
    {
        Query query = mock(Query.class);
        List<Object> value = Arrays.asList("r1", "r2");
        BaseObject baseObject = mock(BaseObject.class);
        DocumentReference value1 = new DocumentReference("xwiki", "XWiki", "Discussion");
        XWikiDocument xWikiDocument = mock(XWikiDocument.class);
        BaseObject messageBaseObject1 = mock(BaseObject.class);
        BaseObject messageBaseObject2 = mock(BaseObject.class);
        List<BaseObject> value2 = Arrays.asList(
            messageBaseObject1,
            null,
            messageBaseObject2
        );

        when(this.messageMetadata.getMessageXClassFullName()).thenReturn("Discussions.Code.MessageClass");
        when(this.queryManager.createQuery(any(), eq(Query.HQL))).thenReturn(query);
        when(query.setLimit(anyInt())).thenReturn(query);
        when(query.setOffset(anyInt())).thenReturn(query);
        when(query.bindValue(any(String.class), any())).thenReturn(query);
        when(query.execute()).thenReturn(value);
        when(this.discussionStoreService.get("discussionReference")).thenReturn(Optional.of(baseObject));
        when(baseObject.getDocumentReference()).thenReturn(value1);
        when(this.xWiki.getDocument(value1, this.xWikiContext)).thenReturn(xWikiDocument);
        when(xWikiDocument.getXObjects(this.messageMetadata.getMessageXClass())).thenReturn(value2);
        when(messageBaseObject1.getStringValue(REFERENCE_NAME)).thenReturn("r2");
        when(messageBaseObject2.getStringValue(REFERENCE_NAME)).thenReturn("r3");

        List<BaseObject> discussionReference =
            this.defaultMessageStoreService.getByDiscussion("discussionReference", 0, 10);
        assertEquals(singletonList(messageBaseObject1), discussionReference);
        verify(query).setLimit(10);
        verify(query).setOffset(0);
        verify(query).bindValue("discussionReference", "discussionReference");
    }
}