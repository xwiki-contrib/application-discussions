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

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.internal.rights.AdminDiscussionRight;
import org.xwiki.contrib.discussions.internal.rights.ReadDiscussionRight;
import org.xwiki.contrib.discussions.internal.rights.WriteDiscussionRight;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.security.authorization.UnableToRegisterRightException;

import com.xpn.xwiki.XWikiContext;

/**
 * Default implementation of {@code DiscussionsRightService}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultDiscussionsRightService implements DiscussionsRightService, Initializable
{
    @Inject
    private AuthorizationManager authorizationManager;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    private Right adminDiscussionRight;

    private Right readDiscussionRight;

    private Right writeDiscussionRight;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.adminDiscussionRight = this.authorizationManager.register(AdminDiscussionRight.INSTANCE);
            this.readDiscussionRight = this.authorizationManager.register(ReadDiscussionRight.INSTANCE);
            this.writeDiscussionRight = this.authorizationManager.register(WriteDiscussionRight.INSTANCE);
        } catch (UnableToRegisterRightException e) {
            throw new InitializationException("Error while initializing the Discussions rights.", e);
        }
    }

    @Override
    public boolean canCreateDiscussion()
    {
        // Currently, we ony allow administrators to create discussions.
        return isAdministrator();
    }

    @Override
    public boolean canCreateDiscussionContext()
    {
        // Currently, we ony allow administrators to create discussion contexts.
        return isAdministrator();
    }

    @Override
    public boolean canReadDiscussion(EntityReference discussion)
    {
        XWikiContext xWikiContext = this.xcontextProvider.get();
        DocumentReference userReference = xWikiContext.getUserReference();
        return this.authorizationManager.hasAccess(this.readDiscussionRight, userReference, discussion);
    }

    @Override
    public boolean canWriteDiscussion(DocumentReference discussion)
    {
        XWikiContext xWikiContext = this.xcontextProvider.get();
        DocumentReference userReference = xWikiContext.getUserReference();
        return this.authorizationManager.hasAccess(this.writeDiscussionRight, userReference, discussion);
    }

    @Override
    public boolean canDeleteMessage(Message message, DocumentReference discussion)
    {
        // Either the current user is the creator of the message and still has write right.
        // Or the current user is an administrator of the discussion.
        DocumentReference userReference = this.xcontextProvider.get().getUserReference();
        String serialize = this.entityReferenceSerializer.serialize(userReference);
        boolean isLocalUser = Objects.equals(message.getActorType(), "user");
        if (isLocalUser) {
            boolean isAuthor = Objects.equals(message.getActorReference(), serialize);
            if (isAuthor && this.canWriteDiscussion(discussion)) {
                return true;
            }
            return this.isAdminDiscussion(discussion);
        }
        return false;
    }

    @Override
    public boolean isAdminDiscussion(DocumentReference discussion)
    {
        XWikiContext xWikiContext = this.xcontextProvider.get();
        DocumentReference userReference = xWikiContext.getUserReference();
        return this.authorizationManager.hasAccess(this.adminDiscussionRight, userReference, discussion);
    }

    private boolean isAdministrator()
    {
        XWikiContext xWikiContext = this.xcontextProvider.get();
        DocumentReference userReference = xWikiContext.getUserReference();
        return this.authorizationManager.hasAccess(Right.ADMIN, userReference, xWikiContext.getWikiReference());
    }
}
