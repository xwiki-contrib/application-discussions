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

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.store.internal.initializer.DiscussionRedirectXClassInitializer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DocumentRedirectionManager}.
 *
 * @version $Id$
 * @since 2.2.3
 */
@ComponentTest
class DocumentRedirectionManagerTest
{
    private static final String REDIRECTION_PARAMETER = "redirection";

    @InjectMockComponents
    private DocumentRedirectionManager documentRedirectionManager;

    @MockComponent
    private Provider<XWikiContext> contextProvider;

    @MockComponent
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @Test
    void handleCreatingRedirection() throws XWikiException
    {
        XWikiDocument document = mock(XWikiDocument.class);
        DiscussionStoreConfigurationParameters configurationParameters =
            mock(DiscussionStoreConfigurationParameters.class);

        this.documentRedirectionManager.handleCreatingRedirection(document, configurationParameters);
        verifyNoInteractions(document);

        when(configurationParameters.containsKey(REDIRECTION_PARAMETER)).thenReturn(true);

        String redirection = "foo";
        when(configurationParameters.get(REDIRECTION_PARAMETER)).thenReturn(redirection);

        XWikiContext context = mock(XWikiContext.class);
        when(this.contextProvider.get()).thenReturn(context);

        BaseObject redirectionObject = mock(BaseObject.class);
        when(document.newXObject(DiscussionRedirectXClassInitializer.XCLASS_REFERENCE, context))
            .thenReturn(redirectionObject);
        this.documentRedirectionManager.handleCreatingRedirection(document, configurationParameters);

        verify(redirectionObject).setStringValue("location", redirection);

        DocumentReference reference = mock(DocumentReference.class);
        redirection = "bar";
        when(this.entityReferenceSerializer.serialize(reference)).thenReturn(redirection);
        when(configurationParameters.get(REDIRECTION_PARAMETER)).thenReturn(reference);

        this.documentRedirectionManager.handleCreatingRedirection(document, configurationParameters);

        verify(redirectionObject).setStringValue("location", redirection);

        String[] stringArray = new String[] { "something" };
        when(configurationParameters.get(REDIRECTION_PARAMETER)).thenReturn(stringArray);

        this.documentRedirectionManager.handleCreatingRedirection(document, configurationParameters);

        verify(redirectionObject).setStringValue("location", stringArray[0]);
    }
}