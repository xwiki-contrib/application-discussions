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

import java.util.Arrays;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.LevelsClass;

import static java.util.Arrays.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.security.internal.XWikiConstants.ALLOW_FIELD_NAME;
import static org.xwiki.security.internal.XWikiConstants.LEVELS_FIELD_NAME;
import static org.xwiki.security.internal.XWikiConstants.LOCAL_CLASSNAME;
import static org.xwiki.security.internal.XWikiConstants.USERS_FIELD_NAME;
import static org.xwiki.security.internal.XWikiConstants.XWIKI_SPACE;

@ComponentTest
class DefaultDiscussionsRightStoreServiceTest
{
    public static final LocalDocumentReference RIGHTS_CLASS_REFERENCE =
        new LocalDocumentReference(XWIKI_SPACE, LOCAL_CLASSNAME);

    @InjectMockComponents
    private DefaultDiscussionsRightStoreService target;

    @MockComponent
    private DiscussionStoreService discussionStoreService;

    @MockComponent
    private Provider<XWikiContext> xWikiContextProvider;

    @MockComponent
    private EntityReferenceSerializer<String> serializer;

    @Mock
    private XWikiContext xwikiContext;

    @Mock
    private XWiki xwiki;

    @Mock
    private XWikiDocument xwikiDocument;

    @BeforeEach
    void setUp()
    {
        when(this.xWikiContextProvider.get()).thenReturn(this.xwikiContext);
        when(this.xwikiContext.getWiki()).thenReturn(this.xwiki);
    }

    @Test
    void setDiscussionRightToUserNoExistingRights() throws Exception
    {
        DocumentReference d1DR = new DocumentReference("xwiki", "XWiki", "D1");
        BaseObject baseObject = mock(BaseObject.class);
        BaseObject newRightBaseObject = mock(BaseObject.class);
        DocumentReference user = new DocumentReference("xwiki", "XWiki", "U1");

        when(this.discussionStoreService.get("ref")).thenReturn(Optional.of(baseObject));
        when(baseObject.getDocumentReference()).thenReturn(d1DR);
        when(this.xwiki.getDocument(d1DR, this.xwikiContext)).thenReturn(this.xwikiDocument);
        when(this.xwikiDocument.getXObjects(RIGHTS_CLASS_REFERENCE)).thenReturn(asList());
        when(this.xwikiDocument.newXObject(RIGHTS_CLASS_REFERENCE, this.xwikiContext)).thenReturn(newRightBaseObject);
        when(this.serializer.serialize(user)).thenReturn("U1");

        this.target.setDiscussionRightToUser("ref", user, "R1");

        verify(newRightBaseObject).setStringValue(LEVELS_FIELD_NAME, "R1");
        verify(newRightBaseObject).setStringValue(USERS_FIELD_NAME, "U1");
        verify(newRightBaseObject).setIntValue(ALLOW_FIELD_NAME, 1);
        verify(this.xwiki).saveDocument(this.xwikiDocument, this.xwikiContext);
    }

    @Test
    void setDiscussionRightToUserNotRightForCurrentUser() throws Exception
    {
        DocumentReference d1DR = new DocumentReference("xwiki", "XWiki", "D1");
        BaseObject discussionBaseObject = mock(BaseObject.class);
        BaseObject newRightBaseObject = mock(BaseObject.class);
        BaseObject existingRightBaseObject = mock(BaseObject.class);
        DocumentReference user = new DocumentReference("xwiki", "XWiki", "U1");

        when(this.discussionStoreService.get("ref")).thenReturn(Optional.of(discussionBaseObject));
        when(discussionBaseObject.getDocumentReference()).thenReturn(d1DR);
        when(this.xwiki.getDocument(d1DR, this.xwikiContext)).thenReturn(this.xwikiDocument);
        when(this.xwikiDocument.getXObjects(RIGHTS_CLASS_REFERENCE)).thenReturn(asList(existingRightBaseObject));
        when(existingRightBaseObject.getStringValue(USERS_FIELD_NAME)).thenReturn("U2");
        when(this.xwikiDocument.newXObject(RIGHTS_CLASS_REFERENCE, this.xwikiContext)).thenReturn(newRightBaseObject);
        when(this.serializer.serialize(user)).thenReturn("U1");

        this.target.setDiscussionRightToUser("ref", user, "R1");

        verify(newRightBaseObject).setStringValue(LEVELS_FIELD_NAME, "R1");
        verify(newRightBaseObject).setStringValue(USERS_FIELD_NAME, "U1");
        verify(newRightBaseObject).setIntValue(ALLOW_FIELD_NAME, 1);
        verify(this.xwiki).saveDocument(this.xwikiDocument, this.xwikiContext);
    }

    @Test
    void setDiscussionRightToUser() throws Exception
    {
        DocumentReference d1DR = new DocumentReference("xwiki", "XWiki", "D1");
        BaseObject discussionBaseObject = mock(BaseObject.class);
        BaseObject existingRightBaseObject = mock(BaseObject.class);
        DocumentReference user = new DocumentReference("xwiki", "XWiki", "U1");

        when(this.discussionStoreService.get("ref")).thenReturn(Optional.of(discussionBaseObject));
        when(discussionBaseObject.getDocumentReference()).thenReturn(d1DR);
        when(this.xwiki.getDocument(d1DR, this.xwikiContext)).thenReturn(this.xwikiDocument);
        when(this.xwikiDocument.getXObjects(RIGHTS_CLASS_REFERENCE)).thenReturn(asList(existingRightBaseObject));
        when(existingRightBaseObject.getStringValue(USERS_FIELD_NAME)).thenReturn("U1");
        when(this.serializer.serialize(user)).thenReturn("U1");
        when(existingRightBaseObject.getStringValue(LEVELS_FIELD_NAME)).thenReturn("R2");

        this.target.setDiscussionRightToUser("ref", user, "R1");

        verify(existingRightBaseObject).setStringValue(LEVELS_FIELD_NAME,
            LevelsClass.getStringFromList(Arrays.asList("R2", "R1"), ","));
        verify(existingRightBaseObject).setIntValue(ALLOW_FIELD_NAME, 1);
        verify(this.xwiki).saveDocument(this.xwikiDocument, this.xwikiContext);
    }

    @Test
    void setDiscussionRightToUserRightsAlreadySet() throws Exception
    {
        DocumentReference d1DR = new DocumentReference("xwiki", "XWiki", "D1");
        BaseObject discussionBaseObject = mock(BaseObject.class);
        BaseObject existingRightBaseObject = mock(BaseObject.class);
        DocumentReference user = new DocumentReference("xwiki", "XWiki", "U1");

        when(this.discussionStoreService.get("ref")).thenReturn(Optional.of(discussionBaseObject));
        when(discussionBaseObject.getDocumentReference()).thenReturn(d1DR);
        when(this.xwiki.getDocument(d1DR, this.xwikiContext)).thenReturn(this.xwikiDocument);
        when(this.xwikiDocument.getXObjects(RIGHTS_CLASS_REFERENCE)).thenReturn(asList(existingRightBaseObject));
        when(existingRightBaseObject.getStringValue(USERS_FIELD_NAME)).thenReturn("U1");
        when(this.serializer.serialize(user)).thenReturn("U1");
        when(existingRightBaseObject.getStringValue(LEVELS_FIELD_NAME)).thenReturn("R1,R2");

        this.target.setDiscussionRightToUser("ref", user, "R1");

        verify(existingRightBaseObject).setStringValue(LEVELS_FIELD_NAME,
            LevelsClass.getStringFromList(Arrays.asList("R1", "R2"), ","));
        verify(existingRightBaseObject).setIntValue(ALLOW_FIELD_NAME, 1);
        verify(this.xwiki).saveDocument(this.xwikiDocument, this.xwikiContext);
    }
}