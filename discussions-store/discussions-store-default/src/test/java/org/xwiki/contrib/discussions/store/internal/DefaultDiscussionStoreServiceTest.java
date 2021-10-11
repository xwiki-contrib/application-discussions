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

import java.util.Collections;
import java.util.Optional;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.store.meta.DiscussionMetadata;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultDiscussionStoreService}.
 *
 * @version $Id$
 * @since 2.0
 */
@ComponentTest
class DefaultDiscussionStoreServiceTest
{
    @InjectMockComponents
    private DefaultDiscussionStoreService storeService;

    @MockComponent
    private Provider<XWikiContext> xcontextProvider;

    @MockComponent
    private DiscussionMetadata discussionMetadata;

    @MockComponent
    private QueryManager queryManager;

    @MockComponent
    private RandomGeneratorService randomGeneratorService;

    @MockComponent
    private DiscussionStoreConfigurationFactory discussionStoreConfigurationFactory;

    @MockComponent
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    @MockComponent
    private DiscussionReferencesResolver discussionReferencesResolver;

    @MockComponent
    private ContextualLocalizationManager localizationManager;

    private XWikiContext context;

    @BeforeEach
    void setup()
    {
        this.context = mock(XWikiContext.class);
        when(this.xcontextProvider.get()).thenReturn(this.context);
    }

    @Test
    void get() throws Exception
    {
        String discussionClass = "DiscussionClass";
        when(this.discussionMetadata.getDiscussionXClassFullName()).thenReturn(discussionClass);

        DiscussionReference discussionReference = mock(DiscussionReference.class);
        String discussionPageRef = "Foo.Bar";
        Query query = mock(Query.class);
        when(this.queryManager.createQuery("FROM doc.object(DiscussionClass) obj where obj.reference = :reference",
            Query.XWQL)).thenReturn(query);
        when(this.discussionReferencesSerializer.serialize(discussionReference)).thenReturn("d1");
        when(query.bindValue("reference", "d1")).thenReturn(query);
        when(query.execute()).thenReturn(Collections.singletonList(discussionPageRef));

        XWiki wiki = mock(XWiki.class);
        when(this.context.getWiki()).thenReturn(wiki);
        XWikiDocument document = mock(XWikiDocument.class);
        when(wiki.getDocument(discussionPageRef, EntityType.DOCUMENT, this.context)).thenReturn(document);

        EntityReference discussionXClass = mock(EntityReference.class);
        when(this.discussionMetadata.getDiscussionXClass()).thenReturn(discussionXClass);

        BaseObject expectedObject = mock(BaseObject.class);
        when(document.getXObject(discussionXClass)).thenReturn(expectedObject);

        assertEquals(Optional.of(expectedObject), this.storeService.get(discussionReference));
        verify(query).bindValue("reference", "d1");
    }

}
