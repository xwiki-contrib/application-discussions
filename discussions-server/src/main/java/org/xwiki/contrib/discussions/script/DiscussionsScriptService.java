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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionContextService;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionsActorServiceResolver;
import org.xwiki.contrib.discussions.MessageService;
import org.xwiki.contrib.discussions.domain.ActorDescriptor;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.domain.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.contrib.discussions.internal.QueryStringService;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.script.service.ScriptService;
import org.xwiki.script.service.ScriptServiceManager;
import org.xwiki.stability.Unstable;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Discussions script service.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
@Named(DiscussionsScriptService.ROLEHINT)
@Component
@Singleton
public class DiscussionsScriptService implements ScriptService
{
    /**
     * The role hint of this component.
     */
    public static final String ROLEHINT = "discussions";

    @Inject
    private DiscussionContextService discussionContextService;

    @Inject
    private DiscussionService discussionService;

    @Inject
    private MessageService messageService;

    @Inject
    private QueryStringService queryStringService;

    @Inject
    private DiscussionsActorServiceResolver actorsServiceResolver;

    @Inject
    private ScriptServiceManager scriptServiceManager;

    @Inject
    private Logger logger;

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    /**
     * Creates a discussion context.
     *
     * @param applicationHint the application in which the discussion has been created.
     * @param name the name
     * @param description the description
     * @param referenceType the entity reference type
     * @param entityReference the entity reference
     * @return the created discussion context
     */
    public DiscussionContext createDiscussionContext(String applicationHint, String name, String description,
        String referenceType, String entityReference)
    {
        if (this.discussionContextService.canCreateDiscussionContext()) {
            return this.discussionContextService.create(applicationHint, name, description,
                new DiscussionContextEntityReference(referenceType, entityReference)).orElse(null);
        } else {
            return null;
        }
    }

    /**
     * Get a discussion context. Creates it if it does not already exist.
     *
     * @param applicationHint the application in which the discussion context has been created.
     * @param name the discussion context name
     * @param description the discussion context description
     * @param referenceType the entity reference type
     * @param entityReference the entity reference
     * @return the request discussion context
     */
    public DiscussionContext getOrCreateDiscussionContext(String applicationHint, String name, String description,
        String referenceType, String entityReference)
    {
        if (this.discussionContextService.canCreateDiscussionContext()) {
            return this.discussionContextService.getOrCreate(applicationHint, name, description,
                    new DiscussionContextEntityReference(referenceType, entityReference))
                .orElse(null);
        } else {
            return null;
        }
    }

    /**
     * Creates a discussion with an URL to the main discussion view page.
     *
     * @param applicationHint the application in which the discussion has been created.
     * @param title the discussion title
     * @param description the discussion description
     * @param mainDocument the main document to view the discussion
     * @return the created discussion
     */
    public Discussion createDiscussion(String applicationHint, String title, String description, String mainDocument)
    {
        return this.discussionService.create(applicationHint, title, description, mainDocument).orElse(null);
    }

    /**
     * Retrieve a discussion by its reference.
     *
     * @param reference the discussion reference
     * @return the discussion, {@code null} if not found
     */
    public Discussion getDiscussion(String reference)
    {
        DiscussionReference discussionReference =
            this.discussionReferencesResolver.resolve(reference, DiscussionReference.class);
        if (this.discussionService.canViewDiscussion(discussionReference)) {
            return this.discussionService.get(discussionReference).orElse(null);
        } else {
            return null;
        }
    }

    /**
     * Retrieve a discussion context by its reference.
     *
     * @param reference the discussion context reference
     * @return the discussion context, {@code null} if not found
     */
    public DiscussionContext getDiscussionContext(String reference)
    {
        DiscussionContextReference discussionContextReference =
            this.discussionReferencesResolver.resolve(reference, DiscussionContextReference.class);
        if (this.discussionContextService.canViewDiscussionContext(discussionContextReference)) {
            return this.discussionContextService.get(discussionContextReference).orElse(null);
        } else {
            return null;
        }
    }

    /**
     * Create a message in a discussion for the current user.
     *
     * @param content the content
     * @param syntax the syntax of the content of the message
     * @param discussion the discussion
     * @return the created message
     */
    public Message createMessage(String content, String syntax, Discussion discussion)
    {
        if (this.discussionService.canWrite(discussion.getReference())) {
            try {
                return this.messageService.create(content, Syntax.valueOf(syntax), discussion.getReference())
                    .orElse(null);
            } catch (ParseException e) {
                this.logger.warn("Malformed syntax [{}]. Cause: [{}].", syntax, getRootCauseMessage(e));
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Return a paginated list of messages of a discussion.
     *
     * @param discussion the discussion
     * @param offset the offset
     * @param limit the limit
     * @return the messages of the discussion
     */
    public List<Message> getMessagesByDiscussion(Discussion discussion, int offset, int limit)
    {
        if (this.discussionService.canViewDiscussion(discussion.getReference())) {
            return this.messageService.getByDiscussion(discussion.getReference(), offset * limit, limit);
        } else {
            return null;
        }
    }

    /**
     * Return the number of messages in a discussion.
     *
     * @param discussion the discussion
     * @return the messages count of the discussion
     */
    public long countMessagesByDiscussion(Discussion discussion)
    {
        if (this.discussionService.canViewDiscussion(discussion.getReference())) {
            return this.messageService.countByDiscussion(discussion);
        } else {
            return 0;
        }
    }

    /**
     * Update a param with newParameterMap values and returns a string representation.
     *
     * @param parameterMap the query string
     * @param newParameterMap the new parameters to overload or add
     * @return the string representation
     */
    public String updateQueryString(Map<String, Object> parameterMap, Map<String, Object> newParameterMap)
    {
        return this.queryStringService.getString(parameterMap, newParameterMap);
    }

    /**
     * Find the discussions linked to exactly the provided list of discussion context reference.
     *
     * @param discussionContextReferences the list of discussion context reference
     * @return the list of discussions
     */
    public List<Discussion> findByDiscussionContexts(List<String> discussionContextReferences)
    {
        return this.discussionService.findByDiscussionContexts(
            discussionContextReferences
                .stream()
                .map(ref -> this.discussionReferencesResolver.resolve(ref, DiscussionContextReference.class))
                .collect(Collectors.toList()));
    }

    /**
     * Links a discussion and a discussion context.
     *
     * @param discussion the discussion
     * @param discussionContext the discussion context
     */
    public void linkDiscussionToDiscussionContext(Discussion discussion, DiscussionContext discussionContext)
    {
        this.discussionContextService.link(discussionContext, discussion);
    }

    /**
     * Unlinks a discussion and a discussion context.
     *
     * @param discussion the discussion
     * @param discussionContext the discussion context
     */
    public void unlinkDiscussionToDiscussionContext(Discussion discussion, DiscussionContext discussionContext)
    {
        this.discussionContextService.unlink(discussionContext, discussion);
    }

    /**
     * Returns an actor descriptor for the provided reference according to its type.
     *
     * @param type the type of the actor
     * @param reference the reference of the actor
     * @return the {@link ActorDescriptor}, or {@code null} in case of error during the resolution
     */
    public ActorDescriptor getActorDescriptor(String type, String reference)
    {
        return this.actorsServiceResolver.get(type).resolve(reference).orElse(null);
    }

    /**
     * @param <S> the type of the {@link ScriptService}
     * @param serviceName the name of the sub {@link ScriptService}
     * @return the {@link ScriptService} or null of none could be found
     */
    @SuppressWarnings("unchecked")
    public <S extends ScriptService> S get(String serviceName)
    {
        return (S) this.scriptServiceManager.get(ROLEHINT + '.' + serviceName);
    }

    /**
     * Safely renders the content of the message.
     *
     * @param messageReference the message reference
     * @return the content of the message rendered in html
     */
    public String renderMessageContent(String messageReference)
    {
        MessageReference reference =
            this.discussionReferencesResolver.resolve(messageReference, MessageReference.class);
        return this.messageService.renderContent(reference);
    }

    /**
     * Checks if the provided discussion reference is linked to the request entity type and entity reference.
     *
     * @param discussionReference a discussion reference
     * @param entityType an entity type
     * @param entityReference an entity reference
     * @return {@code true} if the discussion is linked to an discussion context with the required entity type and
     *     entity reference
     */
    public boolean hasDiscussionContext(String discussionReference, String entityType, String entityReference)
    {
        DiscussionReference reference =
            this.discussionReferencesResolver.resolve(discussionReference, DiscussionReference.class);
        return this.discussionService.findByEntityReferences(entityType, singletonList(entityReference), null, null)
            .stream()
            .anyMatch(it -> it.getReference().equals(reference));
    }

    /**
     * Returns the first discussion found which is link to a discussion context with the provided entity type and entity
     * reference.
     *
     * @param entityType the discussion context entity type
     * @param entityReference the discussion context entity reference
     * @return a discussion linked to the request discussion context
     * @since 1.1
     */
    @Unstable
    public Discussion getDiscussionByDiscussionContext(String entityType, String entityReference)
    {
        List<Discussion> discussions =
            this.discussionService.findByEntityReferences(entityType, singletonList(entityReference), 0, 1);
        Discussion discussion;
        if (discussions.isEmpty()) {
            discussion = null;
        } else {
            discussion = discussions.get(0);
        }

        return discussion;
    }
}
