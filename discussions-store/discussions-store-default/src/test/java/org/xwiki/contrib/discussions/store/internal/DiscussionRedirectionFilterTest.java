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

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.store.internal.initializer.DiscussionRedirectXClassInitializer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiRequest;
import com.xpn.xwiki.web.XWikiResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DiscussionRedirectionFilter}.
 *
 * @since 2.5
 * @version $Id$
 */
@ComponentTest
class DiscussionRedirectionFilterTest
{
    @InjectMockComponents
    private DiscussionRedirectionFilter discussionRedirectionFilter;

    @MockComponent
    private DocumentReferenceResolver<String> resolver;

    @Test
    void redirect() throws XWikiException, IOException
    {
        XWikiContext context = mock(XWikiContext.class, "mockContext");
        when(context.getAction()).thenReturn("download");
        assertFalse(this.discussionRedirectionFilter.redirect(context));

        XWikiDocument document = mock(XWikiDocument.class);
        when(context.getDoc()).thenReturn(document);
        when(context.getAction()).thenReturn("view");
        assertFalse(this.discussionRedirectionFilter.redirect(context));

        BaseObject baseObject = mock(BaseObject.class);
        when(document.getXObject(DiscussionRedirectXClassInitializer.XCLASS_REFERENCE)).thenReturn(baseObject);
        assertFalse(this.discussionRedirectionFilter.redirect(context));

        when(baseObject.getStringValue(DiscussionRedirectXClassInitializer.LOCATION_FIELD)).thenReturn("");
        assertFalse(this.discussionRedirectionFilter.redirect(context));

        WikiReference wikiReference = new WikiReference("foo");
        when(context.getWikiReference()).thenReturn(wikiReference);

        when(baseObject.getStringValue(DiscussionRedirectXClassInitializer.LOCATION_FIELD)).thenReturn("someLocation");
        EntityReference documentReference = mock(DocumentReference.class, "ref1");
        when(this.resolver.resolve("someLocation", wikiReference)).thenReturn((DocumentReference) documentReference);
        XWiki xwiki = mock(XWiki.class);
        when(context.getWiki()).thenReturn(xwiki);
        XWikiRequest xWikiRequest = mock(XWikiRequest.class);
        when(context.getRequest()).thenReturn(xWikiRequest);
        when(xWikiRequest.getQueryString()).thenReturn("queryStringValue");

        when(xwiki.getURL(documentReference, "view", "queryStringValue", null, context))
            .thenReturn("myRedirectUrl");
        XWikiResponse xWikiResponse = mock(XWikiResponse.class);
        when(context.getResponse()).thenReturn(xWikiResponse);
        assertTrue(this.discussionRedirectionFilter.redirect(context));
        verify(xWikiResponse).sendRedirect("myRedirectUrl");
    }
}