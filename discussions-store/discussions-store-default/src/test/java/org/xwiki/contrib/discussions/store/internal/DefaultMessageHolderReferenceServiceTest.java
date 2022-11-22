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

import javax.inject.Provider;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultMessageHolderReferenceService}
 *
 * @version $Id$
 */
@ComponentTest
class DefaultMessageHolderReferenceServiceTest
{
    private static final String MESSAGE_HOLDER_REFERENCE_SESSION_ATTRIBUTE = "xwikiDiscussionsMessageHolderReferences";

    @InjectMockComponents
    private DefaultMessageHolderReferenceService messageHolderReferenceService;

    @MockComponent
    private Provider<XWikiContext> contextProvider;

    @MockComponent
    private PageHolderReferenceFactory pageHolderReferenceFactory;

    private XWikiContext context;
    private HttpSession httpSession;

    @BeforeEach
    void setup()
    {
        this.context = mock(XWikiContext.class);
        when(this.contextProvider.get()).thenReturn(this.context);

        XWikiRequest xWikiRequest = mock(XWikiRequest.class);
        when(this.context.getRequest()).thenReturn(xWikiRequest);

        this.httpSession = mock(HttpSession.class);
        when(xWikiRequest.getSession()).thenReturn(this.httpSession);
    }

    @Test
    void getNextMessageHolderReference()
    {
        String sessionId = "someId45";
        when(this.httpSession.getId()).thenReturn(sessionId);

        DiscussionReference discussionReference = mock(DiscussionReference.class);
        DiscussionStoreConfigurationParameters configurationParameters =
            mock(DiscussionStoreConfigurationParameters.class);

        String applicationHint = "someHint";
        when(discussionReference.getApplicationHint()).thenReturn(applicationHint);

        DocumentReference expectedReference = mock(DocumentReference.class);
        when(this.pageHolderReferenceFactory
            .createPageHolderReference(PageHolderReferenceFactory.DiscussionEntity.MESSAGE,
                "", applicationHint, discussionReference, configurationParameters)).thenReturn(expectedReference);
        doAnswer(invocationOnMock -> {
            MessageHolderReferenceSession session = invocationOnMock.getArgument(1);
            assertEquals(sessionId, session.getSessionId());
            return null;
        }).when(this.httpSession).setAttribute(eq(MESSAGE_HOLDER_REFERENCE_SESSION_ATTRIBUTE),
            any(MessageHolderReferenceSession.class));

        assertEquals(expectedReference, this.messageHolderReferenceService
            .getNextMessageHolderReference(discussionReference, configurationParameters));

        verify(this.httpSession).setAttribute(eq(MESSAGE_HOLDER_REFERENCE_SESSION_ATTRIBUTE), any(
            MessageHolderReferenceSession.class));

        MessageHolderReferenceSession messageHolderReferenceSession = new MessageHolderReferenceSession(sessionId);
        messageHolderReferenceSession.getMessageHolders().put(discussionReference, expectedReference);

        when(this.httpSession.getAttribute(MESSAGE_HOLDER_REFERENCE_SESSION_ATTRIBUTE))
            .thenReturn(messageHolderReferenceSession);
        assertEquals(expectedReference, this.messageHolderReferenceService
            .getNextMessageHolderReference(discussionReference, configurationParameters));

        verify(this.httpSession).setAttribute(eq(MESSAGE_HOLDER_REFERENCE_SESSION_ATTRIBUTE), any(
            MessageHolderReferenceSession.class));
    }

    @Test
    void consumeReference()
    {
        String sessionId = "someId45";
        when(this.httpSession.getId()).thenReturn(sessionId);

        DiscussionReference discussionReference = mock(DiscussionReference.class);
        DocumentReference registeredReference = mock(DocumentReference.class);

        MessageHolderReferenceSession messageHolderReferenceSession = new MessageHolderReferenceSession(sessionId);
        messageHolderReferenceSession.getMessageHolders().put(discussionReference, registeredReference);

        when(this.httpSession.getAttribute(MESSAGE_HOLDER_REFERENCE_SESSION_ATTRIBUTE))
            .thenReturn(messageHolderReferenceSession);

        // should not have any impact since it's not the registered reference
        this.messageHolderReferenceService.consumeReference(discussionReference, mock(DocumentReference.class));

        assertEquals(registeredReference, messageHolderReferenceSession.getMessageHolders().get(discussionReference));

        this.messageHolderReferenceService.consumeReference(discussionReference, registeredReference);
        assertTrue(messageHolderReferenceSession.getMessageHolders().isEmpty());
    }
}