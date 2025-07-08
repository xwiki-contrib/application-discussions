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
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionException;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.MessageService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.domain.references.ActorReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.contrib.discussions.server.DiscussionMessageRequestCreator;
import org.xwiki.contrib.discussions.server.DiscussionServerException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.wysiwyg.converter.RequestParameterConversionResult;
import org.xwiki.wysiwyg.converter.RequestParameterConverter;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.EditForm;

import static org.xwiki.rendering.syntax.Syntax.XWIKI_2_0;

/**
 * Default implementation of {@link DiscussionMessageRequestCreator}.
 *
 * @version $Id$
 * @since 2.1
 */
@Component
@Singleton
public class DefaultDiscussionMessageRequestCreator implements DiscussionMessageRequestCreator
{
    @Inject
    private DiscussionService discussionService;

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    @Inject
    private MessageService messageService;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @Inject
    private RequestParameterConverter requestParameterConverter;

    @Override
    public Message createMessage(HttpServletRequest request) throws DiscussionServerException
    {
        String serializedReference = request.getParameter(DISCUSSION_REFERENCE_PARAM);
        if (StringUtils.isBlank(serializedReference)) {
            throw new DiscussionServerException(HttpServletResponse.SC_BAD_REQUEST,
                "The discussion reference has not been provided.");
        }
        DiscussionReference discussionReference =
            this.discussionReferencesResolver.resolve(serializedReference, DiscussionReference.class);
        Optional<Discussion> discussionOptional = this.discussionService.get(discussionReference);
        Discussion discussion =
            discussionOptional.orElseThrow(() -> new DiscussionServerException(HttpServletResponse.SC_NOT_FOUND,
                String.format("Cannot find discussion with reference [%s]", serializedReference)));
        try {
            return this.createMessage(discussion, request);
        } catch (DiscussionException e) {
            throw new DiscussionServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error when creating "
                + "the message", e);
        }
    }

    private Message createMessage(Discussion discussion, HttpServletRequest request)
        throws DiscussionServerException, DiscussionException
    {
        String content = getContent(request);
        Syntax syntax = getSyntax(request);
        EditForm editForm = this.prepareForm(request);
        DiscussionStoreConfigurationParameters parameters = new DiscussionStoreConfigurationParameters();
        request.getParameterMap().forEach((key, value) -> {
            if (key.startsWith(STORE_CONFIGURATION_PARAMETER_PREFIX)) {
                String parameterKey = key.substring(STORE_CONFIGURATION_PARAMETER_PREFIX.length());
                parameters.put(parameterKey, value);
            }
        });

        // Handle temporary uploads
        List<String> temporaryUploadedFiles = editForm.getTemporaryUploadedFiles();
        parameters.put(TEMPORARY_UPLOADED_ATTACHMENTS, temporaryUploadedFiles);

        String serializedReplyTo = request.getParameter(REPLY_TO_PARAMETER);
        DocumentReference author = this.contextProvider.get().getUserReference();
        String serializedAuthorReference = this.entityReferenceSerializer.serialize(author);
        ActorReference actorReference = new ActorReference("user", serializedAuthorReference);

        Message replyToMessage = null;
        if (!StringUtils.isEmpty(serializedReplyTo)) {
            MessageReference replyTo =
                this.discussionReferencesResolver.resolve(serializedReplyTo, MessageReference.class);
            Optional<Message> replyToReference = this.messageService.getByReference(replyTo);
            if (replyToReference.isPresent()) {
                replyToMessage = replyToReference.get();
            }
        }

        Message message;
        if (replyToMessage == null) {
            message = this.messageService
                .create(content, syntax, discussion.getReference(), actorReference, true, parameters);
        } else {
            message = this.messageService
                .createReplyTo(content, syntax, replyToMessage, actorReference, true, parameters);
        }
        return message;
    }

    private String getContent(HttpServletRequest request) throws DiscussionServerException
    {
        RequestParameterConversionResult conversionResult = this.requestParameterConverter.convert(request);
        // We throw an exception only in case of conversion errors on the content parameter:
        // we don't care if there's other conversions errors as we do not need other parameters here.
        if (conversionResult.getErrors().containsKey(CONTENT_PARAMETER)) {
            throw new DiscussionServerException(HttpServletResponse.SC_BAD_REQUEST,
                "Error when performing conversion of content.", conversionResult.getErrors().get(CONTENT_PARAMETER));
        }
        return conversionResult.getRequest().getParameter(CONTENT_PARAMETER);
    }

    private Syntax getSyntax(HttpServletRequest request)
    {
        Syntax syntax;
        if (request.getParameter(CONTENT_SYNTAX_PARAMETER) != null) {
            try {
                syntax = Syntax.valueOf(request.getParameter(CONTENT_SYNTAX_PARAMETER));
            } catch (ParseException e) {
                syntax = XWIKI_2_0;
            }
        } else {
            syntax = XWIKI_2_0;
        }
        return syntax;
    }

    private EditForm prepareForm(HttpServletRequest request)
    {
        EditForm editForm = new EditForm();
        editForm.setRequest(request);
        editForm.readRequest();
        return editForm;
    }
}
