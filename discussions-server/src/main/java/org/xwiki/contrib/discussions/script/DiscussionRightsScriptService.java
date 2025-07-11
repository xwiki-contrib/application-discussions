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
package org.xwiki.contrib.discussions.script;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionsRightService;
import org.xwiki.contrib.discussions.MessageService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.security.authorization.RuleState;

/**
 * Script service dedicated to the discussions rights.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("discussions.rights")
@Singleton
public class DiscussionRightsScriptService implements ScriptService
{
    @Inject
    private DiscussionService discussionService;

    @Inject
    private MessageService messageService;

    @Inject
    private DiscussionsRightService discussionsRightService;

    @Inject
    private ContextualAuthorizationManager authorizationManager;

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    /**
     * @param discussionReference the discussion reference
     * @return {@code true} if the current user can read the discussion, {@code false} otherwise
     */
    public boolean canReadDiscussion(String discussionReference)
    {
        DiscussionReference reference =
            this.discussionReferencesResolver.resolve(discussionReference, DiscussionReference.class);
        return this.discussionService.canRead(reference);
    }

    /**
     * @param discussionReference the discussion reference
     * @return {@code true} if the current user can write in the discussion, {@code false} otherwise
     */
    public boolean canWriteDiscussion(String discussionReference)
    {
        DiscussionReference reference =
            this.discussionReferencesResolver.resolve(discussionReference, DiscussionReference.class);
        return this.discussionService.canWrite(reference);
    }

    /**
     * @param message the message
     * @return {@code true} if the current user can remove the message, {@code false} otherwise
     */
    public boolean canDeleteMessage(Message message)
    {
        return this.messageService.canDelete(message);
    }

    /**
     * Allows a user to read a discussion. This operation requires the programming right.
     *
     * @param discussion a discussion
     * @param user a user
     * @param allow use {@code true} to allow the right and {@code false} to deny it.
     */
    public void setRead(Discussion discussion, DocumentReference user, boolean allow)
    {
        if (this.authorizationManager.hasAccess(Right.PROGRAM)) {
            RuleState state = (allow) ? RuleState.ALLOW : RuleState.DENY;
            this.discussionsRightService.setRead(discussion, user, state);
        }
    }

    /**
     * Allows a user to write on a discussion. This operation requires the programming right.
     *
     * @param discussion a discussion
     * @param user a user
     * @param allow use {@code true} to allow the right and {@code false} to deny it.
     */
    public void setWrite(Discussion discussion, DocumentReference user, boolean allow)
    {
        if (this.authorizationManager.hasAccess(Right.PROGRAM)) {
            RuleState state = (allow) ? RuleState.ALLOW : RuleState.DENY;
            this.discussionsRightService.setWrite(discussion, user, state);
        }
    }
}
