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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.store.DiscussionContextStoreService;
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
import static org.mockito.Mockito.verify;
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
    private DiscussionContextStoreService discussionContextStoreService;

    @MockComponent
    private DiscussionsRightService discussionsRightService;

    @MockComponent
    private DiscussionReferencesResolver discussionReferencesResolver;

    private DiscussionReference discussionReference;

    @BeforeEach
    void setup()
    {
        this.discussionReference = new DiscussionReference("hint", "reference");
    }

    @Test
    void createCreateFail()
    {
        DiscussionStoreConfigurationParameters parameters = new DiscussionStoreConfigurationParameters();
        when(this.discussionsRightService.canCreateDiscussion()).thenReturn(true);
        when(this.discussionStoreService.create("hint", "title", "description", null, parameters))
            .thenReturn(Optional.empty());

        Optional<Discussion> discussion =
            this.defaultDiscussionService.create("hint", "title", "description", "XWiki.Doc", parameters);

        assertEquals(Optional.empty(), discussion);
    }

    @Test
    void create()
    {
        DiscussionStoreConfigurationParameters parameters = new DiscussionStoreConfigurationParameters();
        Date updateDate = new Date();
        DiscussionReference discussionReference = new DiscussionReference("hint", "reference");
        Discussion discussion = new Discussion(discussionReference, "title", "description", updateDate, "XWiki.Doc");
        BaseObject baseObject = mock(BaseObject.class);

        DocumentReference value = new DocumentReference("xwiki", "a", "b");
        when(baseObject.getDocumentReference()).thenReturn(value);
        when(baseObject.getStringValue(REFERENCE_NAME)).thenReturn("reference");
        when(this.discussionReferencesResolver.resolve("reference", DiscussionReference.class))
            .thenReturn(discussionReference);
        when(baseObject.getStringValue(TITLE_NAME)).thenReturn("title");
        when(baseObject.getStringValue(DESCRIPTION_NAME)).thenReturn("description");
        when(baseObject.getDateValue(UPDATE_DATE_NAME)).thenReturn(updateDate);
        when(baseObject.getStringValue(MAIN_DOCUMENT_NAME)).thenReturn("XWiki.Doc");
        when(this.discussionsRightService.canCreateDiscussion()).thenReturn(true);
        when(this.discussionsRightService.canReadDiscussion(value)).thenReturn(true);
        when(this.discussionStoreService.create("hint", "title", "description", "XWiki.Doc", parameters))
            .thenReturn(Optional.of(this.discussionReference));
        when(this.discussionStoreService.get(this.discussionReference))
            .thenReturn(Optional.of(baseObject));

        Optional<Discussion> discussionOpt =
            this.defaultDiscussionService.create("hint", "title", "description", "XWiki.Doc", parameters);

        assertEquals(Optional.of(discussion), discussionOpt);
    }

    @Test
    void getDoesNotExist()
    {
        when(this.discussionStoreService.get(new DiscussionReference("hint", "reference")))
            .thenReturn(Optional.empty());
        Optional<Discussion> discussion = this.defaultDiscussionService
            .get(new DiscussionReference("hint", "reference"));
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
        when(this.discussionReferencesResolver.resolve("reference", DiscussionReference.class))
            .thenReturn(discussionReference);
        when(this.discussionStoreService.get(discussionReference))
            .thenReturn(Optional.of(discussionBaseObject));
        when(this.discussionsRightService.canReadDiscussion(dr)).thenReturn(true);

        Optional<Discussion> discussion = this.defaultDiscussionService.get(discussionReference);

        assertEquals(Optional.of(new Discussion(discussionReference, "title", "description", updateDate, null)),
            discussion);
    }

    @Test
    void canReadNotFound()
    {
        when(this.discussionStoreService.get(this.discussionReference)).thenReturn(Optional.empty());

        boolean reference = this.defaultDiscussionService.canRead(this.discussionReference);

        assertFalse(reference);
    }

    @Test
    void canReadNotAllowed()
    {
        BaseObject baseObject = mock(BaseObject.class);
        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "Discussion");
        when(baseObject.getDocumentReference()).thenReturn(documentReference);

        when(this.discussionStoreService.get(this.discussionReference)).thenReturn(Optional.of(baseObject));
        when(this.discussionsRightService.canReadDiscussion(documentReference)).thenReturn(false);

        boolean reference = this.defaultDiscussionService.canRead(this.discussionReference);

        assertFalse(reference);
    }

    @Test
    void canRead()
    {
        BaseObject baseObject = mock(BaseObject.class);
        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "Discussion");
        when(baseObject.getDocumentReference()).thenReturn(documentReference);

        when(this.discussionStoreService.get(this.discussionReference)).thenReturn(Optional.of(baseObject));
        when(this.discussionsRightService.canReadDiscussion(documentReference)).thenReturn(true);

        boolean reference = this.defaultDiscussionService.canRead(this.discussionReference);

        assertTrue(reference);
    }

    @Test
    void canWriteNotFound()
    {
        when(this.discussionStoreService.get(this.discussionReference)).thenReturn(Optional.empty());

        boolean reference = this.defaultDiscussionService.canWrite(this.discussionReference);

        assertFalse(reference);
    }

    @Test
    void canWriteNotAllowed()
    {
        BaseObject baseObject = mock(BaseObject.class);
        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "Discussion");
        when(baseObject.getDocumentReference()).thenReturn(documentReference);

        when(this.discussionStoreService.get(this.discussionReference)).thenReturn(Optional.of(baseObject));
        when(this.discussionsRightService.canWriteDiscussion(documentReference)).thenReturn(false);

        boolean reference = this.defaultDiscussionService.canWrite(this.discussionReference);

        assertFalse(reference);
    }

    @Test
    void canWrite()
    {
        BaseObject baseObject = mock(BaseObject.class);
        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "Discussion");
        when(baseObject.getDocumentReference()).thenReturn(documentReference);

        when(this.discussionStoreService.get(this.discussionReference)).thenReturn(Optional.of(baseObject));
        when(this.discussionsRightService.canWriteDiscussion(documentReference)).thenReturn(true);

        boolean reference = this.defaultDiscussionService.canWrite(this.discussionReference);

        assertTrue(reference);
    }

    @Test
    void getOrCreate()
    {
        DiscussionStoreConfigurationParameters parameters = new DiscussionStoreConfigurationParameters();
        Date updateDate = new Date();
        DiscussionReference discussionReference = new DiscussionReference("hint", "reference");
        Discussion discussion = new Discussion(discussionReference, "title", "description", updateDate, "XWiki.Doc");
        BaseObject baseObject = mock(BaseObject.class);

        DocumentReference value = new DocumentReference("xwiki", "a", "b");
        when(baseObject.getDocumentReference()).thenReturn(value);
        when(baseObject.getStringValue(REFERENCE_NAME)).thenReturn("reference");
        when(this.discussionReferencesResolver.resolve("reference", DiscussionReference.class))
            .thenReturn(discussionReference);
        when(baseObject.getStringValue(TITLE_NAME)).thenReturn("title");
        when(baseObject.getStringValue(DESCRIPTION_NAME)).thenReturn("description");
        when(baseObject.getDateValue(UPDATE_DATE_NAME)).thenReturn(updateDate);
        when(baseObject.getStringValue(MAIN_DOCUMENT_NAME)).thenReturn("XWiki.Doc");
        when(this.discussionsRightService.canCreateDiscussion()).thenReturn(true);
        when(this.discussionsRightService.canReadDiscussion(value)).thenReturn(true);
        when(this.discussionStoreService.create("hint", "title", "description", null, parameters))
            .thenReturn(Optional.of(this.discussionReference));
        when(this.discussionStoreService.get(this.discussionReference))
            .thenReturn(Optional.of(baseObject));

        DiscussionContextReference ref1 = mock(DiscussionContextReference.class);
        DiscussionContextReference ref2 = mock(DiscussionContextReference.class);
        List<DiscussionContextReference> contextReferenceList = Arrays.asList(ref1, ref2);

        when(this.discussionStoreService.findByDiscussionContexts(contextReferenceList))
            .thenReturn(Collections.emptyList());
        Optional<Discussion> discussionOpt = this.defaultDiscussionService
            .getOrCreate("hint", "title", "description", contextReferenceList, parameters);

        assertEquals(Optional.of(discussion), discussionOpt);
        verify(this.discussionStoreService).findByDiscussionContexts(contextReferenceList);
        verify(this.discussionStoreService).link(this.discussionReference, ref1);
        verify(this.discussionStoreService).link(this.discussionReference, ref2);
        verify(this.discussionContextStoreService).link(ref1, this.discussionReference);
        verify(this.discussionContextStoreService).link(ref2, this.discussionReference);
    }
}