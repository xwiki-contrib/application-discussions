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

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.store.DiscussionContextMetadataStoreService;
import org.xwiki.contrib.discussions.store.DiscussionContextStoreService;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.DESCRIPTION_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.ENTITY_REFERENCE_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.NAME_NAME;
import static org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata.REFERENCE_NAME;

/**
 * Test of {@link DefaultDiscussionContextService}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class DefaultDiscussionContextServiceTest
{
    @InjectMockComponents
    private DefaultDiscussionContextService defaultDiscussionContextService;

    @MockComponent
    private DiscussionContextStoreService discussionContextStoreService;

    @MockComponent
    private DiscussionsRightService discussionsRightService;

    @MockComponent
    private DiscussionReferencesResolver discussionReferencesResolver;

    @MockComponent
    private DiscussionContextMetadataStoreService discussionContextMetadataStoreService;

    @Test
    void createCreateFail()
    {
        DiscussionStoreConfigurationParameters parameters = new DiscussionStoreConfigurationParameters();
        when(this.discussionsRightService.canCreateDiscussionContext()).thenReturn(true);
        when(this.discussionContextStoreService.create("hint", "name", "description",
            new DiscussionContextEntityReference("referenceType", "entityReference"), parameters))
            .thenReturn(Optional.empty());

        Optional<DiscussionContext> discussionContext =
            this.defaultDiscussionContextService.create("hint", "name", "description",
                new DiscussionContextEntityReference("referenceType", "entityReference"), parameters);

        assertEquals(Optional.empty(), discussionContext);
    }

    @Test
    void create()
    {
        DiscussionStoreConfigurationParameters parameters = new DiscussionStoreConfigurationParameters();
        when(this.discussionsRightService.canCreateDiscussionContext()).thenReturn(true);
        when(this.discussionContextStoreService.create("hint", "name", "description",
            new DiscussionContextEntityReference("referenceType", "entityReference"), parameters))
            .thenReturn(Optional.of(new DiscussionContextReference("hint", "reference")));

        Optional<DiscussionContext> discussionContext =
            this.defaultDiscussionContextService.create("hint", "name", "description",
                new DiscussionContextEntityReference("referenceType", "entityReference"), parameters);

        assertEquals(
            Optional.of(new DiscussionContext(new DiscussionContextReference("hint", "reference")
                , "name", "description",
                new DiscussionContextEntityReference("referenceType", "entityReference"))),
            discussionContext);
    }

    @Test
    void get()
    {
        DiscussionContextReference reference = mock(DiscussionContextReference.class);
        when(this.discussionContextStoreService.get(reference)).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), this.defaultDiscussionContextService.get(reference));

        BaseObject baseObject = mock(BaseObject.class);
        when(this.discussionContextStoreService.get(reference)).thenReturn(Optional.of(baseObject));

        when(baseObject.getStringValue(REFERENCE_NAME)).thenReturn("someReference");
        when(this.discussionReferencesResolver.resolve("someReference", DiscussionContextReference.class))
            .thenReturn(reference);

        String contextName = "someName";
        String description = "some description";
        String entityReferenceType = "someType";
        String entityReferenceName = "42foo";
        when(baseObject.getStringValue(NAME_NAME)).thenReturn(contextName);
        when(baseObject.getStringValue(DESCRIPTION_NAME)).thenReturn(description);
        when(baseObject.getStringValue(ENTITY_REFERENCE_TYPE_NAME)).thenReturn(entityReferenceType);
        when(baseObject.getStringValue(ENTITY_REFERENCE_NAME)).thenReturn(entityReferenceName);

        DiscussionContext expectedContext = new DiscussionContext(reference,
            contextName,
            description,
            new DiscussionContextEntityReference(entityReferenceType, entityReferenceName));

        XWikiDocument ownerDoc = mock(XWikiDocument.class);
        when(baseObject.getOwnerDocument()).thenReturn(ownerDoc);

        assertEquals(Optional.of(expectedContext), this.defaultDiscussionContextService.get(reference));
        verify(this.discussionContextMetadataStoreService).loadMetadata(ownerDoc, expectedContext);
    }
}