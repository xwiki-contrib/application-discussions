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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.store.DiscussionStoreService;
import org.xwiki.contrib.discussions.store.DiscussionsRightsStoreService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.security.authorization.RuleState;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.LevelsClass;
import com.xpn.xwiki.objects.classes.UsersClass;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.security.internal.XWikiConstants.ALLOW_FIELD_NAME;
import static org.xwiki.security.internal.XWikiConstants.GROUPS_FIELD_NAME;
import static org.xwiki.security.internal.XWikiConstants.LEVELS_FIELD_NAME;
import static org.xwiki.security.internal.XWikiConstants.LOCAL_CLASSNAME;
import static org.xwiki.security.internal.XWikiConstants.USERS_FIELD_NAME;
import static org.xwiki.security.internal.XWikiConstants.XWIKI_SPACE;

/**
 * Default implementation of {@link DiscussionsRightsStoreService}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultDiscussionsRightStoreService implements DiscussionsRightsStoreService
{
    @Inject
    private DiscussionStoreService discussionStoreService;

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Inject
    private EntityReferenceSerializer<String> serializer;

    @Inject
    private Logger logger;

    @Override
    public void setDiscussionRightToUser(DiscussionReference discussionReference, DocumentReference user,
        String rightName, RuleState state)
    {
        this.discussionStoreService.get(discussionReference).ifPresent(d -> {
            XWikiContext context = this.xWikiContextProvider.get();
            try {
                XWiki wiki = context.getWiki();
                XWikiDocument document = wiki.getDocument(d.getDocumentReference(), context);

                EntityReference rightsClassReference = new LocalDocumentReference(XWIKI_SPACE, LOCAL_CLASSNAME);
                List<BaseObject> xObjects =
                    document.getXObjects(rightsClassReference);
                String userReference;
                String groupReference;
                Optional<BaseObject> rightsObject;
                userReference = this.serializer.serialize(user);
                groupReference = null;
                rightsObject = xObjects.stream()
                    .filter(obj -> UsersClass.getListFromString(obj.getStringValue(USERS_FIELD_NAME))
                        .stream()
                        .anyMatch(userId -> Objects.equals(userId, userReference)))
                    .findAny();

                setDiscussionRightToUser(rightName, document, rightsClassReference, userReference, null, rightsObject,
                    state);
            } catch (XWikiException e) {
                this.logger
                    .warn("Failed to set right [{}] to user [{}] on document [{}]. Cause: [{}].", rightName, user,
                        discussionReference, getRootCauseMessage(e));
            }
        });
    }

    private void setDiscussionRightToUser(String rightName, XWikiDocument document,
        EntityReference rightsClassReference, String userReference, String groupReference,
        Optional<BaseObject> rightsObject, RuleState state) throws XWikiException
    {
        XWikiContext context = this.xWikiContextProvider.get();
        XWiki wiki = context.getWiki();
        int allowFieldValue = (state == RuleState.ALLOW) ? 1 : 0;
        if (rightsObject.isPresent()) {
            // add the the existing base object
            BaseObject obj = rightsObject.get();
            List<String> levels =
                LevelsClass.getListFromString(obj.getStringValue(LEVELS_FIELD_NAME));
            if (!levels.contains(rightName)) {
                levels.add(rightName);
            }
            obj.setStringValue(LEVELS_FIELD_NAME, LevelsClass.getStringFromList(levels, ","));
            obj.setIntValue(ALLOW_FIELD_NAME, allowFieldValue);
            wiki.saveDocument(document, context);
        } else {
            // create and add a new base object
            BaseObject obj = document.newXObject(rightsClassReference, context);
            obj.setStringValue(LEVELS_FIELD_NAME, rightName);
            if (userReference != null) {
                obj.setStringValue(USERS_FIELD_NAME, userReference);
            }
            if (groupReference != null) {
                obj.setStringValue(GROUPS_FIELD_NAME, groupReference);
            }
            obj.setIntValue(ALLOW_FIELD_NAME, allowFieldValue);
            wiki.saveDocument(document, context);
        }
    }
}
