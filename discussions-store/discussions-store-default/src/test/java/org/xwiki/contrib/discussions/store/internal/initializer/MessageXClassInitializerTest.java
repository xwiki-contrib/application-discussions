package org.xwiki.contrib.discussions.store.internal.initializer;/*
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata;
import org.xwiki.contrib.discussions.store.meta.DiscussionMetadata;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;

import static com.xpn.xwiki.objects.classes.ListClass.DISPLAYTYPE_INPUT;
import static com.xpn.xwiki.objects.classes.ListClass.FREE_TEXT_ALLOWED;
import static com.xpn.xwiki.objects.classes.TextAreaClass.EditorType.WYSIWYG;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DISCUSSION_CONTEXTS_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DISCUSSION_CONTEXTS_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.MAIN_DOCUMENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.MAIN_DOCUMENT_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.TITLE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.TITLE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CREATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CREATE_DATE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.DISCUSSION_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.DISCUSSION_REFERENCE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.PINED_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.PINED_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REPLY_TO_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REPLY_TO_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.STATES_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.STATES_PRETTY_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.UPDATE_DATE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.UPDATE_DATE_PRETTY_NAME;

/**
 * Test of {@link MessageXClassInitializer}.
 *
 * @version $Id$
 * @since 1.1
 */
@ComponentTest
class MessageXClassInitializerTest
{
    public static final DocumentReference SUPERADMIN = new DocumentReference("xwiki", "XWiki", "superadmin");

    @InjectMockComponents
    private MessageXClassInitializer messageXClassInitializer;

    @MockComponent
    private DiscussionContextMetadata discussionContextMetadata;

    @MockComponent
    private WikiDescriptorManager wikiDescriptorManager;

    @BeforeEach
    void setUp()
    {
        when(this.wikiDescriptorManager.getMainWikiId()).thenReturn("xwiki");
    }

    @Test
    void updateDocumentIsNew()
    {
        XWikiDocument document = mock(XWikiDocument.class);
        BaseClass xClass = mock(BaseClass.class);

        when(document.isNew()).thenReturn(true);
        when(document.getXClass()).thenReturn(xClass);

        boolean needsUpdate = this.messageXClassInitializer.updateDocument(document);

        assertTrue(needsUpdate);
        verify(document).setHidden(true);
        verify(xClass).addTextField(REFERENCE_NAME, REFERENCE_PRETTY_NAME, Integer.MAX_VALUE);
        verify(xClass).addTextAreaField(CONTENT_NAME, CONTENT_PRETTY_NAME, 10, 10, TextAreaClass.EditorType.WYSIWYG);
        verify(xClass).addTextField(DISCUSSION_REFERENCE_NAME, DISCUSSION_REFERENCE_PRETTY_NAME, Integer.MAX_VALUE);
        verify(xClass).addTextField(AUTHOR_TYPE_NAME, AUTHOR_TYPE_PRETTY_NAME, Integer.MAX_VALUE);
        verify(xClass).addTextField(AUTHOR_REFERENCE_NAME, AUTHOR_REFERENCE_PRETTY_NAME, Integer.MAX_VALUE);
        verify(xClass).addDateField(UPDATE_DATE_NAME, UPDATE_DATE_PRETTY_NAME);
        verify(xClass).addDateField(CREATE_DATE_NAME, CREATE_DATE_PRETTY_NAME);
        verify(xClass)
            .addStaticListField(STATES_NAME, STATES_PRETTY_NAME, 1, true, true, "", DISPLAYTYPE_INPUT, ",", "",
                FREE_TEXT_ALLOWED, false);
        verify(xClass).addTextField(REPLY_TO_NAME, REPLY_TO_PRETTY_NAME, Integer.MAX_VALUE);
        verify(xClass).addBooleanField(PINED_NAME, PINED_PRETTY_NAME);
        verify(document).setAuthorReference(SUPERADMIN);
    }

    @Test
    void updateDocumentNotNewButWrongAuthor()
    {
        XWikiDocument document = mock(XWikiDocument.class);
        when(document.isNew()).thenReturn(false);
        when(document.getAuthorReference()).thenReturn(null);
        when(document.getCreatorReference()).thenReturn(null);

        boolean needsUpdate = this.messageXClassInitializer.updateDocument(document);

        assertTrue(needsUpdate);

        verify(document, never()).getXClass();
        verify(document).setAuthorReference(SUPERADMIN);
        verify(document).setCreatorReference(SUPERADMIN);
    }

    @Test
    void updateDocumentNothingToUpdate()
    {
        XWikiDocument document = mock(XWikiDocument.class);
        when(document.isNew()).thenReturn(false);
        when(document.getAuthorReference()).thenReturn(SUPERADMIN);
        when(document.getCreatorReference()).thenReturn(SUPERADMIN);

        boolean needsUpdate = this.messageXClassInitializer.updateDocument(document);

        assertFalse(needsUpdate);

        verify(document, never()).getXClass();
        verify(document, never()).setAuthorReference(any());
        verify(document, never()).setCreatorReference(any());
    }
}