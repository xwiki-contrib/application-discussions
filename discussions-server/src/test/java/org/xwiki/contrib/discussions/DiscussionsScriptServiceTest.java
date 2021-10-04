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
package org.xwiki.contrib.discussions;

import java.util.Date;

import static java.util.Collections.*;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.internal.QueryStringService;
import org.xwiki.contrib.discussions.script.DiscussionsScriptService;
import org.xwiki.script.service.ScriptServiceManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

/**
 * Test of {@link DiscussionsScriptService}.
 *
 * @version $Id$
 * @since 1.1
 */
@ComponentTest
class DiscussionsScriptServiceTest
{
    @InjectMockComponents
    private DiscussionsScriptService discussionsScriptService;

    @MockComponent
    private DiscussionContextService discussionContextService;

    @MockComponent
    private DiscussionService discussionService;

    @MockComponent
    private MessageService messageService;

    @MockComponent
    private QueryStringService queryStringService;

    @MockComponent
    private DiscussionsActorServiceResolver actorsServiceResolver;

    @MockComponent
    private ScriptServiceManager scriptServiceManager;

    @Test
    void getDiscussionByDiscussionContext()
    {
        Discussion expected = new Discussion(new DiscussionReference("hint", "discussionReference"),
            "discussionTitle", "discussionDescription", new Date(), "discussionMainDocument");

        when(
            this.discussionService.findByEntityReferences("testEntityType", singletonList("typeEntityReference"), 0, 1))
            .thenReturn(singletonList(expected));

        Discussion discussion =
            this.discussionsScriptService.getDiscussionByDiscussionContext("testEntityType", "typeEntityReference");

        assertEquals(expected, discussion);
    }

    @Test
    void getDiscussionByDiscussionContextNotFound()
    {
        when(
            this.discussionService.findByEntityReferences("testEntityType", singletonList("typeEntityReference"), 0, 1))
            .thenReturn(emptyList());

        Discussion discussion =
            this.discussionsScriptService.getDiscussionByDiscussionContext("testEntityType", "typeEntityReference");

        assertNull(discussion);
    }
}