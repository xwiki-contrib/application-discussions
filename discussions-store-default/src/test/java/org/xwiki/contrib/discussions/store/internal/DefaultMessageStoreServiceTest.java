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

import java.util.Optional;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.meta.MessageMetadata;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.query.QueryManager;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
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

    private DocumentReferenceResolver<EntityReference> currentReferenceDocumentReferenceResolver;

    @BeforeEach
    void setUp(MockitoComponentManager componentManager) throws Exception
    {
        when(this.xcontextProvider.get()).thenReturn(this.xWikiContext);
        when(this.xWikiContext.getWiki()).thenReturn(this.xWiki);
        this.currentReferenceDocumentReferenceResolver =
            componentManager.registerMockComponent(DocumentReferenceResolver.TYPE_REFERENCE, "current");
        com.xpn.xwiki.web.Utils.setComponentManager(componentManager);
    }

    @Test
    void createWrongDiscussion()
    {
        when(this.discussionStoreService.get("discussionReference")).thenReturn(Optional.empty());
        Optional<String> reference = this.defaultMessageStoreService.create("content", "authorType", "authorReference",
            "discussionReference");
        assertFalse(reference.isPresent());
        assertEquals(1, this.logCapture.size());
        assertEquals(Level.WARN, this.logCapture.getLogEvent(0).getLevel());
        assertEquals("Discussion [discussionReference] not found when creating a Message.",
            this.logCapture.getMessage(0));
    }

//    @Test
//    void create() throws Exception
//    {
//        BaseObject value = new BaseObject();
//        DocumentReference reference1 = new DocumentReference("xwiki", "XWiki", "DiscussionObject");
//        value.setDocumentReference(reference1);
//        XWikiDocument xWikiDocument = mock(XWikiDocument.class);
//
//        when(this.discussionStoreService.get("discussionReference")).thenReturn(Optional.of(value));
//        when(this.xWiki.getDocument(reference1, this.xWikiContext)).thenReturn(xWikiDocument);
//        when(this.randomGeneratorService.randomString()).thenReturn("randomString");
//        DocumentReference messageXClassDocumentReference = new DocumentReference("xwiki", "XWiki", "MessageClass");
//        when(this.messageMetadata.getMessageXClass())
//            .thenReturn(messageXClassDocumentReference);
//        HashMap<DocumentReference, List<BaseObject>> xobjects = new HashMap<>();
//        xobjects.put(messageXClassDocumentReference, emptyList());
//        when(xWikiDocument.getXObjects()).thenReturn(xobjects);
//
//        // Maybe replace this document by one that already exist
//        DocumentReference value1 = new DocumentReference("x", "y", "z");
//        when(this.currentReferenceDocumentReferenceResolver.resolve(any(), any())).thenReturn(value1);
//        BaseClass value2 = new BaseClass();
//        
//        when(this.xWiki.getXClass(value1, this.xWikiContext)).thenReturn(value2);
//
//        Optional<String> reference = this.defaultMessageStoreService.create("content", "authorType", "authorReference",
//            "discussionReference");
//        assertFalse(reference.isPresent());
//        assertEquals(1, this.logCapture.size());
//        assertEquals(Level.WARN, this.logCapture.getLogEvent(0).getLevel());
//        assertEquals("Discussion [discussionReference] not found when creating a Message.",
//            this.logCapture.getMessage(0));
//    }

    @Test
    void getByDiscussion()
    {
    }
}