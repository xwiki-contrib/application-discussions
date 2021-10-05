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
package org.xwiki.contrib.discussions.messagestream.script;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionContextService;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.messagestream.internal.DiscussionsFollowersService;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;
import org.xwiki.user.group.GroupException;
import org.xwiki.user.group.GroupManager;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.contrib.discussions.messagestream.internal.DiscussionMessageStreamConfiguration.DISCUSSION_MESSAGESTREAM_HINT;

/**
 * Discussions Message Stream script service.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
@Named("discussions-messagestream")
@Component
@Singleton
public class DiscussionsMessageStreamScriptService implements ScriptService
{
    private static final String MESSAGESTREAM_USER = "messagestream-user";

    private static final String MESSAGESTREAM_EMITTER = "messagestream-emitter";

    @Inject
    private DiscussionContextService discussionContextService;

    @Inject
    private GroupManager groupManager;

    @Inject
    private DocumentReferenceResolver<String> resolver;

    @Inject
    private EntityReferenceSerializer<String> serializer;

    @Inject
    private DiscussionsFollowersService discussionsFollowersService;

    @Inject
    private Logger logger;

    /**
     * Initializes the discussion contexts for a public discussion.
     *
     * @param author the user reference of the author of the discussion
     * @return the list of discussion contexts to attach to the public discussion
     */
    public List<DiscussionContext> initializeContextPublic(String author)
    {
        ArrayList<DiscussionContext> discussionContexts = new ArrayList<>();
        initializeAuthor(author, discussionContexts);
        this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
                new DiscussionContextEntityReference(MESSAGESTREAM_USER, "*"))
            .ifPresent(discussionContexts::add);
        return discussionContexts;
    }

    /**
     * Initializes the discussion contexts for a discussion to the user's followers.
     *
     * @param author the user reference of the author of the discussion
     * @return the list of discussion contexts to attach to the public discussion
     */
    public List<DiscussionContext> initializeContextFollowers(String author)
    {
        ArrayList<DiscussionContext> discussionContexts = new ArrayList<>();
        initializeAuthor(author, discussionContexts);
        String serializedAuthor = this.serializer.serialize(this.resolver.resolve(author));
        for (String follower : this.discussionsFollowersService.getFollowers(serializedAuthor)) {
            this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
                    new DiscussionContextEntityReference(MESSAGESTREAM_USER, follower))
                .ifPresent(discussionContexts::add);
        }
        return discussionContexts;
    }

    /**
     * Initializes the discussion contexts for a discussion to the selected users.
     *
     * @param author the user reference of the author of the discussion
     * @param users the selected list of users
     * @return the list of discussion contexts to attach to the public discussion
     */
    public List<DiscussionContext> initializeContextUsers(String author, List<String> users)
    {
        ArrayList<DiscussionContext> discussionContexts = new ArrayList<>();
        initializeAuthor(author, discussionContexts);
        for (String user : users) {
            if (!"".equals(user)) {
                String serialize = this.serializer.serialize(this.resolver.resolve(user));
                this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
                        new DiscussionContextEntityReference(MESSAGESTREAM_USER, serialize))
                    .ifPresent(discussionContexts::add);
            }
        }
        return discussionContexts;
    }

    /**
     * Initializes the discussion contexts for a discussion to the users of the selected groups.
     *
     * @param author the user reference of the author of the discussion
     * @param groups the selected list of groups
     * @return the list of discussion contexts to attach to the public discussion
     */
    public List<DiscussionContext> initializeContextGroups(String author, List<String> groups)
    {
        ArrayList<DiscussionContext> discussionContexts = new ArrayList<>();
        initializeAuthor(author, discussionContexts);

        groups.stream()
            .filter(it -> !"".equals(it))
            .flatMap(it -> {
                try {
                    return this.groupManager.getMembers(this.resolver.resolve(it), true).stream();
                } catch (GroupException e) {
                    this.logger.warn("Failed to get the list of members for group [{}]. Cause: [{}].", it,
                        getRootCauseMessage(e));
                    return Stream.empty();
                }
            }).collect(Collectors.toSet()).forEach(it -> this.discussionContextService
            .getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
                new DiscussionContextEntityReference(MESSAGESTREAM_USER, this.serializer.serialize(it)))
            .ifPresent(discussionContexts::add));
        return discussionContexts;
    }

    private void initializeAuthor(String author, ArrayList<DiscussionContext> discussionContexts)
    {
        this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
                new DiscussionContextEntityReference(MESSAGESTREAM_EMITTER, author))
            .ifPresent(discussionContexts::add);
        this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, "", "",
                new DiscussionContextEntityReference(MESSAGESTREAM_USER, author))
            .ifPresent(discussionContexts::add);
    }
}
