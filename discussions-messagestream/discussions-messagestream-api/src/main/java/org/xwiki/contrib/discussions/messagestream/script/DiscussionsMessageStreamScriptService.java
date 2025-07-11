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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionContextService;
import org.xwiki.contrib.discussions.DiscussionException;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.messagestream.internal.DiscussionsFollowersService;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.script.service.ScriptService;
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
@Named("discussions-messagestream")
@Component
@Singleton
public class DiscussionsMessageStreamScriptService implements ScriptService
{
    private static final String MESSAGESTREAM_USER = "messagestream-user";

    private static final String MESSAGESTREAM_EMITTER = "messagestream-emitter";

    private static final String USERS_CONTEXT_TITLE_KEY = "discussion.messagestream.context.users.title";
    private static final String USERS_CONTEXT_DESCRIPTION_KEY = "discussion.messagestream.context.users.description";

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
    private ContextualLocalizationManager localizationManager;

    @Inject
    private Logger logger;

    /**
     * Initializes the discussion contexts for a public discussion.
     *
     * @param author the user reference of the author of the discussion
     * @return the list of discussion contexts to attach to the public discussion
     */
    public List<DiscussionContext> initializeContextPublic(String author) throws DiscussionException
    {
        ArrayList<DiscussionContext> discussionContexts = new ArrayList<>();
        initializeAuthor(author, discussionContexts);
        String title =
            this.localizationManager.getTranslationPlain("discussion.messagestream.context.public.title", author);
        String description =
            this.localizationManager.getTranslationPlain("discussion.messagestream.context.public.description", author);
        DiscussionContext discussionContext =
            this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, title, description,
                new DiscussionContextEntityReference(MESSAGESTREAM_USER, "*"),
                new DiscussionStoreConfigurationParameters());
        discussionContexts.add(discussionContext);
        return discussionContexts;
    }

    /**
     * Initializes the discussion contexts for a discussion to the user's followers.
     *
     * @param author the user reference of the author of the discussion
     * @return the list of discussion contexts to attach to the public discussion
     */
    public List<DiscussionContext> initializeContextFollowers(String author) throws DiscussionException
    {
        ArrayList<DiscussionContext> discussionContexts = new ArrayList<>();
        initializeAuthor(author, discussionContexts);
        String serializedAuthor = this.serializer.serialize(this.resolver.resolve(author));
        String title =
            this.localizationManager.getTranslationPlain("discussion.messagestream.context.followers.title", author);
        String description = this.localizationManager
            .getTranslationPlain("discussion.messagestream.context.followers.description", author);
        for (String follower : this.discussionsFollowersService.getFollowers(serializedAuthor)) {
            DiscussionContext discussionContext =
                this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, title, description,
                    new DiscussionContextEntityReference(MESSAGESTREAM_USER, follower),
                    new DiscussionStoreConfigurationParameters());
            discussionContexts.add(discussionContext);
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
    public List<DiscussionContext> initializeContextUsers(String author, List<String> users) throws DiscussionException
    {
        ArrayList<DiscussionContext> discussionContexts = new ArrayList<>();
        initializeAuthor(author, discussionContexts);
        for (String user : users) {
            if (!"".equals(user)) {
                String title =
                    this.localizationManager.getTranslationPlain(USERS_CONTEXT_TITLE_KEY);
                String description = this.localizationManager
                    .getTranslationPlain(USERS_CONTEXT_DESCRIPTION_KEY, author, user);
                String serialize = this.serializer.serialize(this.resolver.resolve(user));
                DiscussionContext discussionContext =
                    this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, title, description,
                        new DiscussionContextEntityReference(MESSAGESTREAM_USER, serialize),
                        new DiscussionStoreConfigurationParameters());
                discussionContexts.add(discussionContext);
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
        throws DiscussionException
    {
        ArrayList<DiscussionContext> discussionContexts = new ArrayList<>();
        initializeAuthor(author, discussionContexts);

        Set<DocumentReference> groupReferences = groups.stream()
            .filter(it -> !"".equals(it))
            .flatMap(it -> {
                try {
                    return this.groupManager.getMembers(this.resolver.resolve(it), true).stream();
                } catch (GroupException e) {
                    this.logger.warn("Failed to get the list of members for group [{}]. Cause: [{}].", it,
                        getRootCauseMessage(e));
                    return Stream.empty();
                }
            }).collect(Collectors.toSet());
        for (DocumentReference group : groupReferences) {
            String serializedUser = this.serializer.serialize(group);
            String title =
                this.localizationManager.getTranslationPlain(USERS_CONTEXT_TITLE_KEY);
            String description = this.localizationManager
                .getTranslationPlain(USERS_CONTEXT_DESCRIPTION_KEY, author, serializedUser);
            DiscussionContext discussionContext = this.discussionContextService
                .getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, title, description,
                    new DiscussionContextEntityReference(MESSAGESTREAM_USER, serializedUser),
                    new DiscussionStoreConfigurationParameters());
            discussionContexts.add(discussionContext);
        }
        return discussionContexts;
    }

    private void initializeAuthor(String author, ArrayList<DiscussionContext> discussionContexts)
        throws DiscussionException
    {
        String title =
            this.localizationManager.getTranslationPlain("discussion.messagestream.context.emitter.title", author);
        String description = this.localizationManager
            .getTranslationPlain("discussion.messagestream.context.emitter.description", author);

        DiscussionContext discussionContext =
            this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, title, description,
                new DiscussionContextEntityReference(MESSAGESTREAM_EMITTER, author),
                new DiscussionStoreConfigurationParameters());
        discussionContexts.add(discussionContext);

        title =
            this.localizationManager.getTranslationPlain("discussion.messagestream.context.personal.title", author);
        description = this.localizationManager
            .getTranslationPlain("discussion.messagestream.context.personal.description", author);

        discussionContext =
            this.discussionContextService.getOrCreate(DISCUSSION_MESSAGESTREAM_HINT, title, description,
                new DiscussionContextEntityReference(MESSAGESTREAM_USER, author),
                new DiscussionStoreConfigurationParameters());
        discussionContexts.add(discussionContext);
    }
}
