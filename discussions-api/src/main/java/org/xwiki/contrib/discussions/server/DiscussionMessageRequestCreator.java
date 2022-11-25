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
package org.xwiki.contrib.discussions.server;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.stability.Unstable;

/**
 * Component responsible of creation of messages based on request information.
 * The idea of this component is to be able to handle creation of messages from request send by the standard editor
 * of messages provided in {@code discussion-macro}.
 *
 * @version $Id$
 * @since 2.1
 */
@Unstable
@Role
public interface DiscussionMessageRequestCreator
{
    /**
     * Mandatory request parameter containing the serialized reference of the discussion to attach the message to.
     */
    String DISCUSSION_REFERENCE_PARAM = "discussionReference";

    /**
     * Mandatory request parameter containing the content of the message to create.
     */
    String CONTENT_PARAMETER = "content";

    /**
     * Recommended request parameter defining the syntax of sent content. Default value is {@code xwiki/2.0} when the
     * parameter is missing.
     */
    String CONTENT_SYNTAX_PARAMETER = "content_syntax";

    /**
     * Optional request parameters prefix for all values that needs to be used in
     * {@link org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters}.
     */
    String STORE_CONFIGURATION_PARAMETER_PREFIX = "storeConfiguration_";

    /**
     * Optional request parameter that needs to contain the message serialized reference of the original message in
     * case of a reply.
     */
    String REPLY_TO_PARAMETER = "replyTo";

    /**
     * Optional request parameters containing the list of temporary files that needs to be saved along with the message.
     */
    String TEMPORARY_UPLOADED_ATTACHMENTS = "temporaryUploadedAttachments";

    /**
     * Creates a message based on the information sent on an http request. The request <strong>must</strong> contain the
     * following parameters:
     * <ul>
     *     <li>{@code discussionReference}: the serialized reference of the discussion the message should be attached
     *     to</li>
     *     <li>{@code content}: the actual content of the message</li>
     * </ul>
     *
     * It <strong>might</strong> contain also those parameters:
     * <ul>
     *     <li>{@code content_syntax}: the syntax of the provided content. If the parameter is not sent, the content is
     *     parsed as {@code xwiki/2.0}</li>
     *     <li>{@code requiresHTMLConversion}: should contains {@code content} if the content needs to be converted from
     *     html first (e.g. if it comes from the WYSIWYG editor). If the parameter is not provided the content won't be
     *     converted</li>
     *     <li>{@code replyTo}: the serialized reference of the message this message is replying to, in case of a
     *     reply</li>
     *     <li>{@code temporaryUploadedAttachments}: the list of temporary uploaded attachments to take into account
     *     when saving the message. The parameter should contain the list of names of files separated by commas</li>
     * </ul>
     *
     * Finally, it <strong>might</strong> also contains several parameters starting with {@code storeConfiguration_}:
     * those are used for the {@link org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters}.
     *
     * This method will throw an {@link DiscussionServerException} if needed parameters are missing or in case of
     * problems to find attached discussions: consumer of the API should rely on the status code recorded in the
     * exception when needing to answer to the server.
     * The returned result is directly linked to the {@link org.xwiki.contrib.discussions.MessageService} API: the
     * {@link Optional} is empty or not depending on it.
     *
     * @param request the actual http request used to create the message
     * @return an optional resulting from a call to the {@link org.xwiki.contrib.discussions.MessageService} API.
     *         When not empty, it contains the actual created message.
     * @throws DiscussionServerException if the request cannot be properly proceed to create the message.
     */
    Optional<Message> createMessage(HttpServletRequest request) throws DiscussionServerException;
}
