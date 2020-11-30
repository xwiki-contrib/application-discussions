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
package org.xwiki.contrib.discussions;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.internal.QueryStringService;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

/**
 * Discussions script service.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
@Named("discussions")
@Component
@Singleton
public class DiscussionsScriptService implements ScriptService
{
    @Inject
    private DiscussionContextService discussionContextService;

    @Inject
    private DiscussionService discussionService;

    @Inject
    private MessageService messageService;
    
    @Inject
    private QueryStringService queryStringService;

    /**
     * Creates a discussion context.
     *
     * @param name the name
     * @param description the description
     * @param referenceType the entity reference type
     * @param entityReference the entity reference
     * @return the created discussion context
     */
    public DiscussionContext createDiscussionContext(String name, String description, String referenceType,
        String entityReference)
    {
        return this.discussionContextService.create(name, description, referenceType, entityReference).orElse(null);
    }

    /**
     * Creates a discussion.
     *
     * @param title the discussion title
     * @param description the discussion description
     * @return the created discussion
     */
    public Discussion createDiscussion(String title, String description)
    {
        return this.discussionService.create(title, description).orElse(null);
    }

    /**
     * Retrieve a discussion by its reference.
     *
     * @param reference the discussion reference
     * @return the discussion, {@code null} if not found
     */
    public Discussion getDiscussion(String reference)
    {
        return this.discussionService.get(reference).orElse(null);
    }

    /**
     * Create a message in a discussion for the current user.
     *
     * @param content the content
     * @param discussion the discussion
     * @return the created message
     */
    public Message createMessage(String content, Discussion discussion)
    {
        return this.messageService.create(content, discussion).orElse(null);
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
        return this.messageService.getByDiscussion(discussion, offset * limit, limit);
    }

    /**
     * Return the number of messages in a discussion.
     *
     * @param discussion the discussion
     * @return the messages count of the discussion
     */
    public long countMessagesByDiscussion(Discussion discussion)
    {
        return this.messageService.countByDiscussion(discussion);
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
}
