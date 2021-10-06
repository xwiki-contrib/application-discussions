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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.script.DiscussionRightsScriptService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

/**
 * Test of {@link DiscussionRightsScriptService}.
 *
 * @version $Id$
 * @since 1.1
 */
@ComponentTest
class DiscussionRightsScriptServiceTest
{
    @InjectMockComponents
    private DiscussionRightsScriptService target;

    @MockComponent
    private DiscussionService discussionService;

    @MockComponent
    private MessageService messageService;

    @MockComponent
    private DiscussionsRightService discussionsRightService;

    @MockComponent
    private ContextualAuthorizationManager authorizationManager;

    @Test
    void setReadNoProgrammingRight()
    {
        DiscussionReference discussionReference = new DiscussionReference("hint", "discussionReference");
        Discussion discussion =
            new Discussion(discussionReference, "discussionTitle", "discussionDescription", new Date(), "XWiki.Test");
        DocumentReference user = new DocumentReference("xwiki", "XWiki", "U1");

        when(this.authorizationManager.hasAccess(Right.PROGRAM)).thenReturn(false);

        this.target.setRead(discussion, user);

        verifyNoInteractions(this.discussionsRightService);
    }

    @Test
    void setRead()
    {
        DiscussionReference discussionReference = new DiscussionReference("hint", "discussionReference");
        Discussion discussion =
            new Discussion(discussionReference, "discussionTitle", "discussionDescription", new Date(), "XWiki.Test");
        DocumentReference user = new DocumentReference("xwiki", "XWiki", "U1");

        when(this.authorizationManager.hasAccess(Right.PROGRAM)).thenReturn(true);

        this.target.setRead(discussion, user);

        verify(this.discussionsRightService).setRead(discussion, user);
    }

    @Test
    void setWriteNoProgrammingRight()
    {
        DiscussionReference discussionReference = new DiscussionReference("hint", "discussionReference");
        Discussion discussion =
            new Discussion(discussionReference, "discussionTitle", "discussionDescription", new Date(), "XWiki.Test");
        DocumentReference user = new DocumentReference("xwiki", "XWiki", "U1");

        when(this.authorizationManager.hasAccess(Right.PROGRAM)).thenReturn(false);

        this.target.setWrite(discussion, user);

        verifyNoInteractions(this.discussionsRightService);
    }

    @Test
    void setWrite()
    {
        DiscussionReference discussionReference = new DiscussionReference("hint", "discussionReference");
        Discussion discussion =
            new Discussion(discussionReference, "discussionTitle", "discussionDescription", new Date(), "XWiki.Test");
        DocumentReference user = new DocumentReference("xwiki", "XWiki", "U1");

        when(this.authorizationManager.hasAccess(Right.PROGRAM)).thenReturn(true);

        this.target.setWrite(discussion, user);

        verify(this.discussionsRightService).setWrite(discussion, user);
    }
}