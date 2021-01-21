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

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.MAIN_DOCUMENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.TITLE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionMetadata.UPDATE_DATE_NAME;

/**
 * Test of {@link DefaultDiscussionService}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class DefaultDiscussionServiceTest
{
    @InjectMockComponents
    private DefaultDiscussionService defaultDiscussionService;

    @MockComponent
    private DiscussionStoreService discussionStoreService;

    @MockComponent
    private DiscussionsRightService discussionsRightService;

    @Test
    void createCreateFail()
    {
        when(this.discussionsRightService.canCreateDiscussion()).thenReturn(true);
        when(this.discussionStoreService.create("title", "description", null))
            .thenReturn(Optional.empty());

        Optional<Discussion> discussion =
            this.defaultDiscussionService.create("title", "description", "XWiki.Doc");

        assertEquals(Optional.empty(), discussion);
    }

    @Test
    void create()
    {
        Date updateDate = new Date();
        Discussion discussion = new Discussion("reference", "title", "description", updateDate, "XWiki.Doc");
        BaseObject baseObject = mock(BaseObject.class);

        DocumentReference value = new DocumentReference("xwiki", "a", "b");
        when(baseObject.getDocumentReference()).thenReturn(value);
        when(baseObject.getStringValue(REFERENCE_NAME)).thenReturn("reference");
        when(baseObject.getStringValue(TITLE_NAME)).thenReturn("title");
        when(baseObject.getStringValue(DESCRIPTION_NAME)).thenReturn("description");
        when(baseObject.getDateValue(UPDATE_DATE_NAME)).thenReturn(updateDate);
        when(baseObject.getStringValue(MAIN_DOCUMENT_NAME)).thenReturn("XWiki.Doc");
        when(this.discussionsRightService.canCreateDiscussion()).thenReturn(true);
        when(this.discussionsRightService.canReadDiscussion(value)).thenReturn(true);
        when(this.discussionStoreService.create("title", "description", "XWiki.Doc"))
            .thenReturn(Optional.of("reference"));
        when(this.discussionStoreService.get("reference")).thenReturn(Optional.of(baseObject));

        Optional<Discussion> discussionOpt =
            this.defaultDiscussionService.create("title", "description", "XWiki.Doc");

        assertEquals(Optional.of(discussion), discussionOpt);
    }

    @Test
    void getDoesNotExist()
    {
        when(this.discussionStoreService.get("reference")).thenReturn(Optional.empty());
        Optional<Discussion> discussion = this.defaultDiscussionService.get("reference");
        assertEquals(Optional.empty(), discussion);
    }

    @Test
    void get()
    {
        DocumentReference dr = new DocumentReference("xwiki", "XWiki", "Discussion");
        BaseObject discussionBaseObject = mock(BaseObject.class);
        Date updateDate = new Date();

        when(discussionBaseObject.getDocumentReference()).thenReturn(dr);
        when(discussionBaseObject.getStringValue(REFERENCE_NAME)).thenReturn("reference");
        when(discussionBaseObject.getStringValue(TITLE_NAME)).thenReturn("title");
        when(discussionBaseObject.getStringValue(DESCRIPTION_NAME)).thenReturn("description");
        when(discussionBaseObject.getDateValue(UPDATE_DATE_NAME)).thenReturn(updateDate);
        when(this.discussionStoreService.get("reference")).thenReturn(Optional.of(discussionBaseObject));
        when(this.discussionsRightService.canReadDiscussion(dr)).thenReturn(true);

        Optional<Discussion> discussion = this.defaultDiscussionService.get("reference");

        assertEquals(Optional.of(new Discussion("reference", "title", "description", updateDate, null)), discussion);
    }

    @Test
    void canReadNotFound()
    {
        when(this.discussionStoreService.get("reference")).thenReturn(Optional.empty());

        boolean reference = this.defaultDiscussionService.canRead("reference");

        assertFalse(reference);
    }

    @Test
    void canReadNotAllowed()
    {
        BaseObject baseObject = mock(BaseObject.class);
        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "Discussion");
        when(baseObject.getDocumentReference()).thenReturn(documentReference);

        when(this.discussionStoreService.get("reference")).thenReturn(Optional.of(baseObject));
        when(this.discussionsRightService.canReadDiscussion(documentReference)).thenReturn(false);

        boolean reference = this.defaultDiscussionService.canRead("reference");

        assertFalse(reference);
    }

    @Test
    void canRead()
    {
        BaseObject baseObject = mock(BaseObject.class);
        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "Discussion");
        when(baseObject.getDocumentReference()).thenReturn(documentReference);

        when(this.discussionStoreService.get("reference")).thenReturn(Optional.of(baseObject));
        when(this.discussionsRightService.canReadDiscussion(documentReference)).thenReturn(true);

        boolean reference = this.defaultDiscussionService.canRead("reference");

        assertTrue(reference);
    }

    @Test
    void canWriteNotFound()
    {
        when(this.discussionStoreService.get("reference")).thenReturn(Optional.empty());

        boolean reference = this.defaultDiscussionService.canWrite("reference");

        assertFalse(reference);
    }

    @Test
    void canWriteNotAllowed()
    {
        BaseObject baseObject = mock(BaseObject.class);
        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "Discussion");
        when(baseObject.getDocumentReference()).thenReturn(documentReference);

        when(this.discussionStoreService.get("reference")).thenReturn(Optional.of(baseObject));
        when(this.discussionsRightService.canWriteDiscussion(documentReference)).thenReturn(false);

        boolean reference = this.defaultDiscussionService.canWrite("reference");

        assertFalse(reference);
    }

    @Test
    void canWrite()
    {
        BaseObject baseObject = mock(BaseObject.class);
        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "Discussion");
        when(baseObject.getDocumentReference()).thenReturn(documentReference);

        when(this.discussionStoreService.get("reference")).thenReturn(Optional.of(baseObject));
        when(this.discussionsRightService.canWriteDiscussion(documentReference)).thenReturn(true);

        boolean reference = this.defaultDiscussionService.canWrite("reference");

        assertTrue(reference);
    }
}