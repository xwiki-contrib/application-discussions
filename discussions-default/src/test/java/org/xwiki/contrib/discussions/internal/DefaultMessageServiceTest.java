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
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.domain.MessageContent;
import org.xwiki.contrib.discussions.domain.references.ActorReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.MessageStoreService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CREATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.DISCUSSION_REFERENCE_NAME;
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

    @MockComponent
    private DiscussionReferencesResolver referencesResolver;

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
        DiscussionReference discussionReference = new DiscussionReference("hint", "reference");
        Discussion discussion = new Discussion(discussionReference, "title", "description", new Date(), null);
        when(this.discussionService.get(discussionReference)).thenReturn(Optional.of(discussion));
        BaseObject discussionBaseObject = mock(BaseObject.class);
        when(this.discussionStoreService.get(discussionReference)).thenReturn(Optional.of(discussionBaseObject));
        DocumentReference discussionDocumentReference = new DocumentReference("xwiki", "XWiki", "DiscussionDoc");
        when(discussionBaseObject.getDocumentReference()).thenReturn(discussionDocumentReference);
        setDiscussionWriteRight(discussionDocumentReference, false);

        Optional<Message> message = this.defaultMessageService.create("content", XWIKI_2_1, discussionReference);

        assertEquals(Optional.empty(), message);
    }

    @Test
    void createAllowed()
    {
        DiscussionReference discussionReference = new DiscussionReference("hint", "reference");
        Discussion discussion = new Discussion(discussionReference, "title", "description", new Date(), "XWiki.Doc");
        when(this.discussionService.get(discussionReference)).thenReturn(Optional.of(discussion));
        BaseObject discussionBaseObject = mock(BaseObject.class);
        when(this.discussionStoreService.get(discussionReference)).thenReturn(Optional.of(discussionBaseObject));
        DocumentReference discussionDocumentReference = new DocumentReference("xwiki", "XWiki", "DiscussionDoc");
        when(discussionBaseObject.getDocumentReference()).thenReturn(discussionDocumentReference);
        setDiscussionWriteRight(discussionDocumentReference, true);
        setDiscussionReadRight(discussionDocumentReference, true);
        MessageReference messageReference = new MessageReference("hint", "messageReference");
        when(this.messageStoreService
            .create("content", XWIKI_2_1, "user", USER_REFERENCE, discussionReference, "title"))
            .thenReturn(Optional.of(messageReference));
        BaseObject messageBaseObject = mock(BaseObject.class);
        when(this.messageStoreService.getByReference(messageReference))
            .thenReturn(Optional.of(messageBaseObject));

        when(messageBaseObject.getStringValue(REFERENCE_NAME)).thenReturn("messageReference");
        when(referencesResolver.resolve("messageReference", MessageReference.class)).thenReturn(messageReference);
        when(messageBaseObject.getLargeStringValue(CONTENT_NAME)).thenReturn("CONTENT_NAME");
        XWikiDocument xWikiDocument = mock(XWikiDocument.class);
        when(messageBaseObject.getOwnerDocument()).thenReturn(xWikiDocument);
        when(xWikiDocument.getSyntax()).thenReturn(XWIKI_2_1);
        when(messageBaseObject.getStringValue(AUTHOR_TYPE_NAME)).thenReturn("AUTHOR_TYPE_NAME");
        when(messageBaseObject.getStringValue(AUTHOR_REFERENCE_NAME)).thenReturn("AUTHOR_REFERENCE_NAME");
        Date createDate = new Date();
        when(messageBaseObject.getDateValue(CREATE_DATE_NAME)).thenReturn(createDate);
        Date updateDate = new Date();
        when(messageBaseObject.getDateValue(UPDATE_DATE_NAME)).thenReturn(updateDate);
        when(messageBaseObject.getStringValue(DISCUSSION_REFERENCE_NAME)).thenReturn("reference");
        when(referencesResolver.resolve("reference", DiscussionReference.class)).thenReturn(discussionReference);

        Optional<Message> message = this.defaultMessageService.create("content", XWIKI_2_1, discussionReference);

        assertEquals(
            Optional.of(new Message(messageReference, new MessageContent("CONTENT_NAME", XWIKI_2_1),
                new ActorReference("AUTHOR_TYPE_NAME", "AUTHOR_REFERENCE_NAME"),
                createDate, updateDate, discussion)),
            message);
    }

    @Test
    void renderContent()
    {
        MessageReference messageReference = new MessageReference("hint", "messageReference");
        BaseObject baseObject = mock(BaseObject.class);
        XWikiDocument xWikiDocument = mock(XWikiDocument.class);
        when(baseObject.getOwnerDocument()).thenReturn(xWikiDocument);
        when(xWikiDocument.getSyntax()).thenReturn(XWIKI_2_1);
        when(this.messageStoreService.getByReference(messageReference)).thenReturn(Optional.of(baseObject));
        when(baseObject.displayView(CONTENT_NAME, this.context)).thenReturn("html result");
        String actual = this.defaultMessageService.renderContent(messageReference);
        assertEquals("html result", actual);
    }

    @Test
    void getByEntityReference()
    {
        DiscussionReference discussionReference = new DiscussionReference("hint", "reference");
        MessageReference messageReference = new MessageReference("hint", "messageReference");
        Discussion discussion =
            new Discussion(discussionReference, "discussionTitle", "discussionDescription", new Date(),
                "discussionMainDocument");
        Date createDate = new Date();
        Date updateDate = new Date();
        Message message = new Message(messageReference, new MessageContent("messageContent", Syntax.XHTML_1_0),
            new ActorReference("messageActorType", "messageActorReference"), createDate, updateDate,
            discussion);
        ObjectReference entityReference = new ObjectReference("Discussion.Message.MessageObject",
            new DocumentReference("xwiki", "XWiki", "MessageTest"));
        BaseObject bo = mock(BaseObject.class);
        XWikiDocument xWikiDocument = mock(XWikiDocument.class);

        when(this.messageStoreService.getByEntityReference(entityReference))
            .thenReturn(Optional.of(bo));
        when(bo.getStringValue(DISCUSSION_REFERENCE_NAME)).thenReturn("discussionReference");
        when(this.referencesResolver.resolve("discussionReference", DiscussionReference.class))
            .thenReturn(discussionReference);
        when(bo.getStringValue(REFERENCE_NAME)).thenReturn("messageReference");
        when(this.referencesResolver.resolve("messageReference", MessageReference.class)).thenReturn(messageReference);
        when(bo.getLargeStringValue(CONTENT_NAME)).thenReturn("messageContent");
        when(bo.getOwnerDocument()).thenReturn(xWikiDocument);
        when(xWikiDocument.getSyntax()).thenReturn(Syntax.XHTML_1_0);
        when(bo.getStringValue(AUTHOR_TYPE_NAME)).thenReturn("messageActorType");
        when(bo.getStringValue(AUTHOR_REFERENCE_NAME)).thenReturn("messageActorReference");
        when(bo.getDateValue(CREATE_DATE_NAME)).thenReturn(createDate);
        when(bo.getDateValue(UPDATE_DATE_NAME)).thenReturn(updateDate);
        when(this.discussionService.get(discussionReference)).thenReturn(Optional.of(discussion));

        Optional<Message> actual =
            this.defaultMessageService.getByEntity(entityReference);
        assertEquals(Optional.of(message), actual);
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