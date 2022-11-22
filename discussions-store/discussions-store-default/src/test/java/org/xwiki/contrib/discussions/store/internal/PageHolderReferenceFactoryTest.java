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

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.store.DiscussionStoreConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PageHolderReferenceFactory}.
 *
 * @version $Id$
 */
@ComponentTest
class PageHolderReferenceFactoryTest
{
    @InjectMockComponents
    private PageHolderReferenceFactory referenceFactory;

    @MockComponent
    private DiscussionStoreConfigurationFactory discussionStoreConfigurationFactory;

    @Test
    void createPageHolderReferenceForDiscussionContext()
    {
        String name = "context42";
        String applicationHint = "someApp";
        DiscussionContextEntityReference reference = mock(DiscussionContextEntityReference.class);
        DiscussionStoreConfigurationParameters parameters = mock(DiscussionStoreConfigurationParameters.class);

        DiscussionStoreConfiguration discussionStoreConfiguration = mock(DiscussionStoreConfiguration.class);
        when(this.discussionStoreConfigurationFactory.getDiscussionStoreConfiguration(applicationHint))
            .thenReturn(discussionStoreConfiguration);

        SpaceReference spaceReference = mock(SpaceReference.class);
        when(discussionStoreConfiguration.getDiscussionContextSpaceStorageLocation(parameters, reference))
            .thenReturn(spaceReference);

        DocumentReference documentReference = this.referenceFactory.createPageHolderReference(
            PageHolderReferenceFactory.DiscussionEntity.DISCUSSION_CONTEXT,
            name, applicationHint, reference, parameters);
        assertNotNull(documentReference);

        assertEquals(spaceReference, documentReference.getParent());
        assertTrue(documentReference.getName().startsWith(name));
    }

    @Test
    void createPageHolderReferenceForDiscussion()
    {
        String applicationHint = "someApp";
        DiscussionStoreConfigurationParameters parameters = mock(DiscussionStoreConfigurationParameters.class);

        DiscussionStoreConfiguration discussionStoreConfiguration = mock(DiscussionStoreConfiguration.class);
        when(this.discussionStoreConfigurationFactory.getDiscussionStoreConfiguration(applicationHint))
            .thenReturn(discussionStoreConfiguration);

        SpaceReference spaceReference = mock(SpaceReference.class);
        when(discussionStoreConfiguration.getDiscussionSpaceStorageLocation(parameters))
            .thenReturn(spaceReference);

        DocumentReference documentReference = this.referenceFactory.createPageHolderReference(
            PageHolderReferenceFactory.DiscussionEntity.DISCUSSION,
            "", applicationHint, null, parameters);
        assertNotNull(documentReference);

        assertEquals(spaceReference, documentReference.getParent());
        assertFalse(documentReference.getName().isEmpty());
    }

    @Test
    void createPageHolderReferenceForMessage()
    {
        String name = "message24";
        String applicationHint = "someApp";
        DiscussionReference reference = mock(DiscussionReference.class);
        DiscussionStoreConfigurationParameters parameters = mock(DiscussionStoreConfigurationParameters.class);

        DiscussionStoreConfiguration discussionStoreConfiguration = mock(DiscussionStoreConfiguration.class);
        when(this.discussionStoreConfigurationFactory.getDiscussionStoreConfiguration(applicationHint))
            .thenReturn(discussionStoreConfiguration);

        SpaceReference spaceReference = mock(SpaceReference.class);
        when(discussionStoreConfiguration.getMessageSpaceStorageLocation(parameters, reference))
            .thenReturn(spaceReference);

        DocumentReference documentReference = this.referenceFactory.createPageHolderReference(
            PageHolderReferenceFactory.DiscussionEntity.MESSAGE,
            name, applicationHint, reference, parameters);
        assertNotNull(documentReference);

        assertEquals(spaceReference, documentReference.getParent());
        assertTrue(documentReference.getName().startsWith(name));
    }
}