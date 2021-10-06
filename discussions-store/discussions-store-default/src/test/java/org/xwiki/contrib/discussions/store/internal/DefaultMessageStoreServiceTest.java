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
import java.util.List;
import java.util.Optional;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.references.ActorReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.contrib.discussions.store.DiscussionStoreConfiguration;
import org.xwiki.contrib.discussions.store.meta.MessageMetadata;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
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

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private MessageMetadata messageMetadata;

    @MockComponent
    private QueryManager queryManager;

    @MockComponent
    private RandomGeneratorService randomGeneratorService;

    @MockComponent
    private DiscussionReferencesResolver discussionReferencesResolver;

    @MockComponent
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    @MockComponent
    private DiscussionStoreConfigurationFactory discussionStoreConfigurationFactory;

    @Mock
    private XWikiContext xWikiContext;

    @Mock
    private XWiki xWiki;

    @BeforeEach
    void setUp()
    {
        when(this.xcontextProvider.get()).thenReturn(this.xWikiContext);
        when(this.xWikiContext.getWiki()).thenReturn(this.xWiki);
    }

    @Test
    void create() throws Exception
    {
        DiscussionReference discussionReference = new DiscussionReference("hint", "discussionReference");
        DiscussionStoreConfiguration discussionStoreConfiguration = mock(DiscussionStoreConfiguration.class);
        when(this.discussionStoreConfigurationFactory.getDiscussionStoreConfiguration("hint"))
            .thenReturn(discussionStoreConfiguration);

        DiscussionStoreConfigurationParameters parameters = new DiscussionStoreConfigurationParameters();
        BaseObject messageBaseObject = mock(BaseObject.class);
        DocumentReference discussionDocumentReference = new DocumentReference("xwiki", "XWiki", "randomString");
        DocumentReference messageXClassDocumentReference = new DocumentReference("xwiki", "XWiki", "MessageClass");
        XWikiDocument document = mock(XWikiDocument.class);
        SpaceReference value = new SpaceReference("xwiki", "Discussions", "Message");
        when(discussionStoreConfiguration.getMessageSpaceStorageLocation(parameters, discussionReference))
            .thenReturn(value);

        when(document.getDocumentReference()).thenReturn(discussionDocumentReference);
        when(document.newXObject(messageXClassDocumentReference, this.xWikiContext)).thenReturn(messageBaseObject);
        when(this.randomGeneratorService.randomString(10)).thenReturn("randomString", "randomString2");
        when(this.messageMetadata.getMessageXClass()).thenReturn(messageXClassDocumentReference);
        when(this.xWiki.getDocument(new DocumentReference("randomString", value), this.xWikiContext))
            .thenReturn(document);
        when(document.isNew()).thenReturn(true);

        MessageReference messageReference = new MessageReference("hint", "randomString");
        when(this.discussionReferencesSerializer.serialize(messageReference)).thenReturn("randomString;hint=hint");
        when(this.discussionReferencesSerializer.serialize(discussionReference))
            .thenReturn("discussionReference;hint=hint");

        Optional<MessageReference> reference = this.defaultMessageStoreService.create("content", XWIKI_2_1,
            new ActorReference("authorType", "authorReference"), discussionReference, "TODO", parameters);
        assertEquals(Optional.of(messageReference), reference);
        assertEquals(0, this.logCapture.size());
        verify(messageBaseObject).set(REFERENCE_NAME, "randomString;hint=hint", this.xWikiContext);
        verify(messageBaseObject).set(AUTHOR_TYPE_NAME, "authorType", this.xWikiContext);
        verify(messageBaseObject).set(AUTHOR_REFERENCE_NAME, "authorReference", this.xWikiContext);
        verify(messageBaseObject).set(CONTENT_NAME, "content", this.xWikiContext);
        verify(messageBaseObject).set(DISCUSSION_REFERENCE_NAME, "discussionReference;hint=hint", this.xWikiContext);
    }

    @Test
    void getByDiscussion() throws Exception
    {
        Query query = mock(Query.class);
        List<Object> value = asList("r1", "r2");
        DocumentReference messageXClass = new DocumentReference("xwiki", "XWiki", "MessageClass");
        XWikiDocument xWikiDocumentR1 = mock(XWikiDocument.class);
        XWikiDocument xWikiDocumentR2 = mock(XWikiDocument.class);
        BaseObject r1MessageObject = mock(BaseObject.class);
        BaseObject r2MessageObject = mock(BaseObject.class);

        when(this.messageMetadata.getMessageXClassFullName()).thenReturn("Discussions.Code.MessageClass");
        when(this.queryManager.createQuery(any(), eq(Query.HQL))).thenReturn(query);
        when(query.setLimit(anyInt())).thenReturn(query);
        when(query.setOffset(anyInt())).thenReturn(query);
        when(query.bindValue(any(String.class), any())).thenReturn(query);
        when(query.execute()).thenReturn(value);
        when(this.messageMetadata.getMessageXClass()).thenReturn(messageXClass);
        when(this.xWiki.getDocument("r1", EntityType.DOCUMENT, this.xWikiContext))
            .thenReturn(xWikiDocumentR1);
        when(this.xWiki.getDocument("r2", EntityType.DOCUMENT, this.xWikiContext))
            .thenReturn(xWikiDocumentR2);
        when(xWikiDocumentR1.getXObject((EntityReference) messageXClass)).thenReturn(r1MessageObject);
        when(xWikiDocumentR2.getXObject((EntityReference) messageXClass)).thenReturn(r2MessageObject);

        List<BaseObject> actual = this.defaultMessageStoreService
            .getByDiscussion(new DiscussionReference("hint", "discussionReference"), 0, 10);

        assertEquals(asList(r1MessageObject, r2MessageObject), actual);
    }

    @Test
    void getByReference() throws Exception
    {
        MessageReference messageReference = new MessageReference("hint", "docRef1");
        when(this.discussionReferencesSerializer.serialize(messageReference)).thenReturn("reference");
        BaseObject messageBaseObject1 = mock(BaseObject.class);
        when(this.messageMetadata.getMessageXClassFullName()).thenReturn("Discussions.Code.MessageClass");
        DocumentReference messageXClass = new DocumentReference("xwiki", "XWiki", "MessageClass");
        when(this.messageMetadata.getMessageXClass()).thenReturn(messageXClass);
        Query query = mock(Query.class);
        when(this.queryManager.createQuery(any(), any())).thenReturn(query);
        when(query.bindValue("reference", "reference")).thenReturn(query);
        when(query.execute()).thenReturn(Collections.singletonList("docRef1"));

        XWikiDocument xWikiDocument = mock(XWikiDocument.class);
        when(this.xWiki.getDocument("docRef1", EntityType.DOCUMENT, this.xWikiContext))
            .thenReturn(xWikiDocument);
        when(xWikiDocument.getXObject((EntityReference) messageXClass)).thenReturn(messageBaseObject1);

        when(this.discussionReferencesResolver.resolve("docRef1", MessageReference.class)).thenReturn(messageReference);
        Optional<BaseObject> actual = this.defaultMessageStoreService.getByReference(messageReference);
        assertEquals(Optional.of(messageBaseObject1), actual);
    }
}