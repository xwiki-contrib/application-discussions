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
package org.xwiki.contrib.discussions.internal.rest;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.rest.DiscussionREST;
import org.xwiki.contrib.discussions.rest.model.CreateDiscussion;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.XWikiRestException;

/**
 * Default implementation of the {@link DiscussionREST} API.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("org.xwiki.contrib.discussions.internal.rest.DefaultDiscussionREST")
public class DefaultDiscussionREST extends XWikiResource implements DiscussionREST
{
    @Inject
    private DiscussionService discussionService;

    @Override
    public Discussion get(String reference) throws XWikiRestException
    {
        return this.discussionService.get(reference)
            .orElseThrow(() -> new XWikiRestException(
                String.format("Discussion with reference=[%s] not found.", reference)));
    }

    @Override
    public Discussion create(CreateDiscussion discussion) throws XWikiRestException
    {
        String title = discussion.getTitle();
        String description = discussion.getDescription();
        return this.discussionService.create(title, description)
            .orElseThrow(() -> new XWikiRestException(
                String.format("Fail to create a discussion with title=[%s], description=[%s]", title, description)));
    }
}
