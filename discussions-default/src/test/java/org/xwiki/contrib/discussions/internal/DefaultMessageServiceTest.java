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
package org.xwiki.contrib.discussions.internal;

import java.util.Date;
import java.util.Optional;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.domain.MessageContent;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.MessageStoreService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CREATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.UPDATE_DATE_NAME;
import static org.xwiki.rendering.syntax.Syntax.XWIKI_2_1;

/**
 * Test of {@link DefaultMessageService}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class DefaultMessageServiceTest
{
    private static final DocumentReference USER_DOCUMENT_REFERENCE = new DocumentReference("xwiki", "XWiki", "User");

    private static final String USER_REFERENCE = "xwiki:XWiki.User";

    @InjectMockComponents
    private DefaultMessageService defaultMessageService;

    @MockComponent
    private MessageStoreService messageStoreService;

    @MockComponent
    private DiscussionService discussionService;

    @MockComponent
    private DiscussionStoreService discussionStoreService;

    @MockComponent
    private Provider<XWikiContext> xcontextProvider;

    @MockComponent
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @MockComponent
    private DiscussionsRightService discussionsRightService;

    @Mock
    private XWikiContext context;

    @BeforeEach
    void setUp()
    {
        when(this.xcontextProvider.get()).thenReturn(this.context);
        when(this.context.getUserReference()).thenReturn(USER_DOCUMENT_REFERENCE);
        when(this.entityReferenceSerializer.serialize(USER_DOCUMENT_REFERENCE)).thenReturn(USER_REFERENCE);
    }

    @Test
    void createDisallowed()
    {
        Discussion discussion = new Discussion("reference", "title", "description", new Date());
        when(this.discussionService.get("reference")).thenReturn(Optional.of(discussion));
        BaseObject discussionBaseObject = mock(BaseObject.class);
        when(this.discussionStoreService.get("reference")).thenReturn(Optional.of(discussionBaseObject));
        DocumentReference discussionDocumentReference = new DocumentReference("xwiki", "XWiki", "DiscussionDoc");
        when(discussionBaseObject.getDocumentReference()).thenReturn(discussionDocumentReference);
        setDiscussionWriteRight(discussionDocumentReference, false);

        Optional<Message> message = this.defaultMessageService.create("content", XWIKI_2_1, "reference");

        assertEquals(Optional.empty(), message);
    }

    @Test
    void createAllowed()
    {
        Discussion discussion = new Discussion("reference", "title", "description", new Date());
        when(this.discussionService.get("reference")).thenReturn(Optional.of(discussion));
        BaseObject discussionBaseObject = mock(BaseObject.class);
        when(this.discussionStoreService.get("reference")).thenReturn(Optional.of(discussionBaseObject));
        DocumentReference discussionDocumentReference = new DocumentReference("xwiki", "XWiki", "DiscussionDoc");
        when(discussionBaseObject.getDocumentReference()).thenReturn(discussionDocumentReference);
        setDiscussionWriteRight(discussionDocumentReference, true);
        setDiscussionReadRight(discussionDocumentReference, true);
        when(this.messageStoreService.create("content", XWIKI_2_1, "user", USER_REFERENCE, "reference"))
            .thenReturn(Optional.of("messageReference"));
        BaseObject messageBaseObject = mock(BaseObject.class);
        when(this.messageStoreService.getByReference("messageReference", "reference"))
            .thenReturn(Optional.of(messageBaseObject));

        when(messageBaseObject.getStringValue(REFERENCE_NAME)).thenReturn("messageReference");
        when(messageBaseObject.getLargeStringValue(CONTENT_NAME)).thenReturn("CONTENT_NAME");
        when(messageBaseObject.getStringValue(AUTHOR_TYPE_NAME)).thenReturn("AUTHOR_TYPE_NAME");
        when(messageBaseObject.getStringValue(AUTHOR_REFERENCE_NAME)).thenReturn("AUTHOR_REFERENCE_NAME");
        Date createDate = new Date();
        when(messageBaseObject.getDateValue(CREATE_DATE_NAME)).thenReturn(createDate);
        Date updateDate = new Date();
        when(messageBaseObject.getDateValue(UPDATE_DATE_NAME)).thenReturn(updateDate);

        Optional<Message> message = this.defaultMessageService.create("content", XWIKI_2_1, "reference");

        assertEquals(
            Optional.of(new Message("messageReference", new MessageContent("CONTENT_NAME", XWIKI_2_1),
                "AUTHOR_TYPE_NAME",
                "AUTHOR_REFERENCE_NAME",
                createDate, updateDate, discussion)),
            message);
    }

    private void setDiscussionWriteRight(DocumentReference discussionDocumentReference, boolean b)
    {
        when(this.discussionsRightService.canWriteDiscussion(discussionDocumentReference)).thenReturn(b);
    }

    private void setDiscussionReadRight(DocumentReference discussionDocumentReference, boolean b)
    {
        when(this.discussionsRightService.canReadDiscussion(discussionDocumentReference)).thenReturn(b);
    }
}