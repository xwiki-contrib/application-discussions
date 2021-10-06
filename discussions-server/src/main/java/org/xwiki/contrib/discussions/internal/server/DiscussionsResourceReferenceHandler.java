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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.container.servlet.ServletResponse;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.MessageService;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.contrib.discussions.internal.DiscussionsResourceReference;
import org.xwiki.csrf.CSRFToken;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.resource.AbstractResourceReferenceHandler;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceReferenceHandlerChain;
import org.xwiki.resource.ResourceReferenceHandlerException;
import org.xwiki.resource.ResourceType;
import org.xwiki.resource.annotations.Authenticate;
import org.xwiki.wysiwyg.converter.HTMLConverter;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.rendering.syntax.Syntax.XWIKI_2_0;

/**
 * Main handler for the Discussions.
 * <p>
 * This handler receive the non-rest http requests of the discussions and process them.
 * <ul>
 *     <li>/create/Message/discussionId/</li>
 * </ul>
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("discussions")
@Singleton
@Authenticate
public class DiscussionsResourceReferenceHandler extends AbstractResourceReferenceHandler<ResourceType>
{
    private static final ResourceType TYPE = new ResourceType("discussions");

    private static final String ORIGINAL_URL_PARAM = "originalURL";

    private static final String REFERENCE_PARAM = "reference";

    private static final String DISCUSSION_REFERENCE_PARAM = "discussionReference";

    private static final String CONTENT_PARAMETER = "content";

    private static final String REQUIRES_HTML_CONVERSION_PARAMETER = "RequiresHTMLConversion";

    private static final String CONTENT_SYNTAX_PARAMETER = "content_syntax";

    @Inject
    private Logger logger;

    @Inject
    private MessageService messageService;

    @Inject
    private DiscussionService discussionService;

    @Inject
    private Container container;

    @Inject
    private HTMLConverter htmlConverter;

    @Inject
    private CSRFToken csrfToken;

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    @Override
    public List<ResourceType> getSupportedResourceReferences()
    {
        return singletonList(TYPE);
    }

    @Override
    public void handle(ResourceReference reference, ResourceReferenceHandlerChain chain)
        throws ResourceReferenceHandlerException
    {
        this.logger.debug("reference = [{}], chain=[{}]", reference, chain);

        DiscussionsResourceReference discussionsResourceReference = (DiscussionsResourceReference) reference;
        HttpServletRequest request =
            ((ServletRequest) this.container.getRequest()).getHttpServletRequest();
        HttpServletResponse response =
            ((ServletResponse) this.container.getResponse()).getHttpServletResponse();

        switch (discussionsResourceReference.getActionType()) {
            case CREATE:
                handleCreate(discussionsResourceReference, request, response);
                break;
            case READ:
                handleRead(discussionsResourceReference);
                break;
            case UPDATE:
                handleUpdate(discussionsResourceReference);
                break;
            case DELETE:
                handleDelete(discussionsResourceReference, request, response);
                break;
            default:
                handleNext(reference, chain);
                return;
        }

        handleNext(reference, chain);
    }

    private void handleDelete(DiscussionsResourceReference discussionsResourceReference, HttpServletRequest request,
        HttpServletResponse response)
    {
        switch (discussionsResourceReference.getDiscussionsEntityType()) {
            case MESSAGE:
                MessageReference messageReference =
                    this.discussionReferencesResolver.resolve(request.getParameter(REFERENCE_PARAM),
                        MessageReference.class);
                this.messageService.delete(messageReference);
                redirect(response, request.getParameter(ORIGINAL_URL_PARAM));
                break;
            case DISCUSSION:
                break;
            case DISCUSSION_CONTEXT:
                break;
            default:
                break;
        }
    }

    private void handleRead(DiscussionsResourceReference discussionsResourceReference)
    {
        switch (discussionsResourceReference.getDiscussionsEntityType()) {
            case MESSAGE:
                break;
            case DISCUSSION:
                break;
            case DISCUSSION_CONTEXT:
                break;
            default:
                break;
        }
    }

    private void handleUpdate(DiscussionsResourceReference discussionsResourceReference)
    {
        switch (discussionsResourceReference.getDiscussionsEntityType()) {
            case MESSAGE:
                break;
            case DISCUSSION:
                break;
            case DISCUSSION_CONTEXT:
                break;
            default:
                break;
        }
    }

    private void handleCreate(DiscussionsResourceReference discussionsResourceReference, HttpServletRequest request,
        HttpServletResponse response)
    {
        switch (discussionsResourceReference.getDiscussionsEntityType()) {
            case MESSAGE:
                createMessage(request, response);
                break;
            case DISCUSSION:
                break;
            case DISCUSSION_CONTEXT:
                break;
            default:
                break;
        }
    }

    private void createMessage(HttpServletRequest request, HttpServletResponse response)
    {

        if (!this.csrfToken.isTokenValid(request.getParameter("form_token"))) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
            } catch (IOException e) {
                this.logger.warn("Failed to return a request error response. Cause: [{}]", getRootCauseMessage(e));
            }
        } else {
            DiscussionReference discussionReference =
                this.discussionReferencesResolver.resolve(request.getParameter(DISCUSSION_REFERENCE_PARAM),
                    DiscussionReference.class);
            this.discussionService
                .get(discussionReference)
                .ifPresent(d -> {
                    String content = getContent(request);
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
                    this.messageService.create(content, syntax, d.getReference())
                        .ifPresent(m -> {
                            String parameter = request.getParameter(ORIGINAL_URL_PARAM);
                            try {
                                URIBuilder uriBuilder = new URIBuilder(parameter);
                                List<NameValuePair> queryParams = uriBuilder
                                    .getQueryParams();
                                String offsetName = request.getParameter("namespace") + "_offset";
                                List<NameValuePair> collect =
                                    queryParams.stream().filter(it -> !it.getName().equals(offsetName))
                                        .collect(Collectors.toList());
                                URI namespace = uriBuilder.clearParameters().setParameters(collect)
                                    .build();
                                redirect(response, namespace.toASCIIString());
                            } catch (URISyntaxException e) {
                                redirect(response, parameter);
                            }
                        });
                });
        }
    }

    private String getContent(HttpServletRequest request)
    {
        String content = request.getParameter(CONTENT_PARAMETER);
        String requiresHTMLConversion = request.getParameter(REQUIRES_HTML_CONVERSION_PARAMETER);
        String syntax = request.getParameter(CONTENT_SYNTAX_PARAMETER);

        String contentClean;
        if (Objects.equals(requiresHTMLConversion, CONTENT_PARAMETER)) {
            contentClean = this.htmlConverter.fromHTML(content, syntax);
        } else {
            contentClean = content;
        }
        return contentClean;
    }

    private void redirect(HttpServletResponse response, String originalURL)
    {
        try {
            response.sendRedirect(originalURL);
        } catch (IOException e) {
            try {
                throw new ResourceReferenceHandlerException(
                    String.format("Failed to redirect to [%s]", originalURL), e);
            } catch (ResourceReferenceHandlerException resourceReferenceHandlerException) {
                resourceReferenceHandlerException.printStackTrace();
            }
        }
    }

    private void handleNext(ResourceReference reference, ResourceReferenceHandlerChain chain)
        throws ResourceReferenceHandlerException
    {
        // Be a good citizen, continue the chain, in case some lower-priority Handler has something to do for this
        // Resource Reference.
        chain.handleNext(reference);
    }
}
