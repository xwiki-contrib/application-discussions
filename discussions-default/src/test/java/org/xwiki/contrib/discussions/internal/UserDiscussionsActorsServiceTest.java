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

import java.net.URI;
import java.util.Optional;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.contrib.discussions.domain.ActorDescriptor;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test of {@link UserDiscussionsActorsService}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class UserDiscussionsActorsServiceTest
{
    @InjectMockComponents
    private UserDiscussionsActorsService userDiscussionsActorsService;

    @MockComponent
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @MockComponent
    private Provider<XWikiContext> xcontextProvider;

    @Mock
    private XWikiContext xWikiContext;

    @Mock
    private XWiki xWiki;

    @BeforeEach
    void setUp()
    {
        when(this.xcontextProvider.get()).thenReturn(this.xWikiContext);
        when(this.xWikiContext.getWiki()).thenReturn(this.xWiki);
    }

    @Test
    void resolve()
    {
        DocumentReference actorDocumentReference = new DocumentReference("xwiki", "XWiki", "ar");
        when(this.documentReferenceResolver.resolve("actorRef"))
            .thenReturn(actorDocumentReference);
        when(this.xWiki.getPlainUserName(actorDocumentReference, this.xWikiContext)).thenReturn("NAME");
        Optional<ActorDescriptor> actual = this.userDiscussionsActorsService.resolve("actorRef");
        ActorDescriptor expected = new ActorDescriptor();
        expected.setName("NAME");
        expected.setLink(URI.create("actorRef"));
        assertEquals(Optional.of(expected), actual);
    }
}