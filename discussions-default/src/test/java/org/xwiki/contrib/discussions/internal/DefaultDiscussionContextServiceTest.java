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
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.domain.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.store.DiscussionContextStoreService;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    @Test
    void createCreateFail()
    {
        when(this.discussionsRightService.canCreateDiscussionContext()).thenReturn(true);
        when(this.discussionContextStoreService.create("name", "description", "referenceType", "entityReference"))
            .thenReturn(Optional.empty());

        Optional<DiscussionContext> discussionContext =
            this.defaultDiscussionContextService.create("name", "description", "referenceType", "entityReference");

        assertEquals(Optional.empty(), discussionContext);
    }

    @Test
    void create()
    {
        when(this.discussionsRightService.canCreateDiscussionContext()).thenReturn(true);
        when(this.discussionContextStoreService.create("name", "description", "referenceType", "entityReference"))
            .thenReturn(Optional.of("reference"));

        Optional<DiscussionContext> discussionContext =
            this.defaultDiscussionContextService.create("name", "description", "referenceType", "entityReference");

        assertEquals(
            Optional.of(new DiscussionContext("reference", "name", "description",
                new DiscussionContextEntityReference("referenceType", "entityReference"))),
            discussionContext);
    }
}