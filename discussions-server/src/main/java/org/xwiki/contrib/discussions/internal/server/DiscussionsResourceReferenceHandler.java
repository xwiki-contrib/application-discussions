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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.container.servlet.ServletResponse;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.MessageService;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.domain.references.MessageReference;
import org.xwiki.contrib.discussions.internal.DiscussionsResourceReference;
import org.xwiki.contrib.discussions.server.DiscussionMessageRequestCreator;
import org.xwiki.contrib.discussions.server.DiscussionServerException;
import org.xwiki.csrf.CSRFToken;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.resource.AbstractResourceReferenceHandler;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceReferenceHandlerChain;
import org.xwiki.resource.ResourceReferenceHandlerException;
import org.xwiki.resource.ResourceType;
import org.xwiki.resource.annotations.Authenticate;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.manager.WikiManagerException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xpn.xwiki.XWikiContext;

import static java.util.Collections.singletonList;

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

    private static final String ASYNC_PARAMETER = "async";

    @Inject
    private Logger logger;

    @Inject
    private MessageService messageService;

    @Inject
    private Container container;

    @Inject
    private CSRFToken csrfToken;

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    @Inject
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    @Inject
    private DiscussionMessageRequestCreator discussionMessageRequestCreator;

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

        if (!this.csrfToken.isTokenValid(request.getParameter("form_token"))) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
            } catch (IOException e) {
                throw new ResourceReferenceHandlerException("Error while sending error response", e);
            }
        } else {
            XWikiContext context = this.contextProvider.get();
            WikiReference currentWiki = null;
            if (discussionsResourceReference.getWikiReference() != null) {
                checkWiki(discussionsResourceReference);
                currentWiki = context.getWikiReference();
                context.setWikiReference(discussionsResourceReference.getWikiReference());
            }
            try {
                switch (discussionsResourceReference.getActionType()) {
                    case CREATE:
                        try {
                            handleCreate(discussionsResourceReference, request, response);
                        } catch (IOException e) {
                            throw new ResourceReferenceHandlerException("Error when handling discussion create action",
                                e);
                        }
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
            } finally {
                if (currentWiki != null) {
                    context.setWikiReference(currentWiki);
                }
            }
        }

        handleNext(reference, chain);
    }

    private void checkWiki(DiscussionsResourceReference discussionsResourceReference)
        throws ResourceReferenceHandlerException
    {
        String wikiName = discussionsResourceReference.getWikiReference().getName();
        try {
            if (!this.wikiDescriptorManager.exists(wikiName)) {
                throw new ResourceReferenceHandlerException(
                    String.format("The provided wiki [%s] does not exist.", wikiName));
            }
        } catch (WikiManagerException e) {
            throw new ResourceReferenceHandlerException(
                String.format("Error while checking if wiki [%s] exists", wikiName), e);
        }
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
        HttpServletResponse response) throws IOException
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

    private void createMessage(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        try {
            Optional<Message> messageOptional = this.discussionMessageRequestCreator.createMessage(request);
            if (messageOptional.isEmpty()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error when creating message");
            } else {
                if (isAsync(request)) {
                    Map<String, String> answer = new LinkedHashMap<>();
                    answer.put("messageReference",
                        this.discussionReferencesSerializer.serialize(messageOptional.get().getReference()));
                    this.answerJSON(response, HttpServletResponse.SC_OK, answer);
                } else {
                    this.handleCreateMessageRedirect(request, response);
                }
            }
        } catch (DiscussionServerException e) {
            response.sendError(e.getStatusCode(), e.getMessage());
            this.logger.debug("Error when creating a message: [{}]", e.getMessage(), e.getCause());
        }
    }

    /**
     * Answer to a request with a JSON content.
     * Note: this method was partially copied from {@link com.xpn.xwiki.web.XWikiAction}.
     *
     * @param response where to send the response
     * @param status the status code to send back.
     * @param answer the content of the JSON answer.
     * @throws IOException in case of error during the serialization of the JSON.
     */
    protected void answerJSON(HttpServletResponse response, int status, Map<String, String> answer) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        XWikiContext context = contextProvider.get();
        String jsonAnswerAsString = mapper.writeValueAsString(answer);
        response.setContentType("application/json");
        response.setContentLength(jsonAnswerAsString.length());
        response.setStatus(status);
        response.setCharacterEncoding(context.getWiki().getEncoding());
        response.getWriter().print(jsonAnswerAsString);
        context.setResponseSent(true);
    }

    private void handleCreateMessageRedirect(HttpServletRequest request, HttpServletResponse response)
    {
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
            this.logger.warn("Error when building URI from parameter [{}]: [{}]", parameter,
                ExceptionUtils.getRootCauseMessage(e));
            redirect(response, parameter);
        }
    }

    private boolean isAsync(HttpServletRequest request)
    {
        return "1".equals(request.getParameter(ASYNC_PARAMETER));
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
