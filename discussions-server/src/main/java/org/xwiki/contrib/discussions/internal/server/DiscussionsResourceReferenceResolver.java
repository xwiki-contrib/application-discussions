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
package org.xwiki.contrib.discussions.internal.server;

import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.internal.DiscussionsActionType;
import org.xwiki.contrib.discussions.internal.DiscussionsEntityType;
import org.xwiki.contrib.discussions.internal.DiscussionsResourceReference;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceType;
import org.xwiki.resource.UnsupportedResourceReferenceException;
import org.xwiki.url.ExtendedURL;
import org.xwiki.url.internal.AbstractResourceReferenceResolver;

import static org.xwiki.contrib.discussions.internal.DiscussionsEntityType.DISCUSSION;
import static org.xwiki.contrib.discussions.internal.DiscussionsEntityType.DISCUSSION_CONTEXT;
import static org.xwiki.contrib.discussions.internal.DiscussionsEntityType.MESSAGE;

/**
 * Resolve an {@link ExtendedURL} to an {@link DiscussionsResourceReference}.
 * <p>
 * Paths:
 * <ul>
 *     <li>/Message/...</li>
 * </ul>
 *
 * @version $Id$
 */
@Component
@Named("discussions")
@Singleton
public class DiscussionsResourceReferenceResolver extends AbstractResourceReferenceResolver
{
    @Override
    public ResourceReference resolve(ExtendedURL representation, ResourceType resourceType,
        Map<String, Object> parameters) throws UnsupportedResourceReferenceException
    {
        List<String> segments = representation.getSegments();
        if (segments.isEmpty()) {
            throw new UnsupportedResourceReferenceException("Unknown action");
        }

        if (segments.size() < 2) {
            throw new UnsupportedResourceReferenceException("Unknown entity");
        }

        DiscussionsActionType actionType = getActionType(segments.get(0));
        DiscussionsEntityType discussionsEntityType = getEntityType(segments.get(1));

        return new DiscussionsResourceReference(actionType, discussionsEntityType);
    }

    private DiscussionsActionType getActionType(String actionTypeSegment) throws UnsupportedResourceReferenceException
    {
        DiscussionsActionType discussionsActionType;
        switch (actionTypeSegment) {
            case "create":
                discussionsActionType = DiscussionsActionType.CREATE;
                break;
            case "read":
                discussionsActionType = DiscussionsActionType.READ;
                break;
            case "update":
                discussionsActionType = DiscussionsActionType.UPDATE;
                break;
            case "delete":
                discussionsActionType = DiscussionsActionType.DELETE;
                break;
            default:
                throw new UnsupportedResourceReferenceException(
                    String.format("Unknown discussion action type [%s]", actionTypeSegment));
        }
        return discussionsActionType;
    }

    private DiscussionsEntityType getEntityType(String discussionEntityTypeSegment)
        throws UnsupportedResourceReferenceException
    {
        DiscussionsEntityType discussionsEntityType;
        switch (discussionEntityTypeSegment) {
            case "Message":
                discussionsEntityType = MESSAGE;
                break;
            case "Discussion":
                discussionsEntityType = DISCUSSION;
                break;
            case "DiscussionContext":
                discussionsEntityType = DISCUSSION_CONTEXT;
                break;
            default:
                throw new UnsupportedResourceReferenceException(
                    String.format("Unknown discussions entity type [%s]", discussionEntityTypeSegment));
        }
        return discussionsEntityType;
    }
}
