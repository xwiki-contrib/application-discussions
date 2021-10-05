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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionsActorService;
import org.xwiki.contrib.discussions.DiscussionsActorServiceResolver;
import org.xwiki.contrib.discussions.MessageService;
import org.xwiki.contrib.discussions.domain.ActorDescriptor;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.rest.DiscussionLiveTableRow;
import org.xwiki.contrib.discussions.rest.DiscussionREST;
import org.xwiki.contrib.discussions.rest.DiscussionUserRow;
import org.xwiki.contrib.discussions.rest.LiveTableResult;
import org.xwiki.contrib.discussions.rest.model.CreateDiscussion;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rest.XWikiRestComponent;
import org.xwiki.rest.XWikiRestException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Default implementation of the {@link DiscussionREST} API.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("org.xwiki.contrib.discussions.internal.rest.DefaultDiscussionREST")
@Singleton
public class DefaultDiscussionREST implements DiscussionREST, XWikiRestComponent
{
    private static final String CAUSE_ERROR_RESPONSE = "Cause: [%s]";

    @Inject
    private DiscussionService discussionService;

    @Inject
    private MessageService messageService;

    @Inject
    private DiscussionsActorServiceResolver discussionsActorServiceResolver;

    @Inject
    @Named("url")
    private EntityReferenceSerializer<String> urlSerializer;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private DiscussionReferencesResolver discussionReferencesResolver;

    @Inject
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    @Inject
    private Logger logger;

    @Override
    public Discussion get(String reference) throws XWikiRestException
    {
        DiscussionReference discussionReference =
            this.discussionReferencesResolver.resolve(reference, DiscussionReference.class);
        return this.discussionService.get(discussionReference)
            .orElseThrow(() -> new XWikiRestException(
                String.format("Discussion with reference=[%s] not found.", reference)));
    }

    @Override
    public Response livetable(String type, String reference, Integer offset, Integer limit, String sort, String dir,
        Integer reqNo, String linkTemplate, Boolean jokerAllowed)
    {
        LiveTableResult<DiscussionLiveTableRow> ltr = new LiveTableResult<>();
        ltr.setOffset(offset);
        ltr.setReqNo(reqNo);
        List<String> references;
        if (jokerAllowed.equals(Boolean.TRUE)) {
            references = Arrays.asList(reference, "*");
        } else {
            references = Arrays.asList(reference);
        }
        ltr.setTotalrows(this.discussionService.countByEntityReferences(type, references));
        ltr.setRows(this.discussionService.findByEntityReferences(type, references, offset - 1, limit)
            .stream()
            .map(d -> {
                DiscussionLiveTableRow discussionLiveTableRow = new DiscussionLiveTableRow();
                discussionLiveTableRow.setTitle(d.getTitle());
                try {
                    discussionLiveTableRow.setTitleUrl(linkTemplate.replace("__REFERENCE__", URLEncoder
                        .encode(this.discussionReferencesSerializer.serialize(d.getReference()), "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    this.logger.warn("Failed to generate the title for discussion [{}]. Cause: [{}]", e,
                        getRootCauseMessage(e));
                }
                discussionLiveTableRow.setUpdateDate(d.getUpdateDate());
                discussionLiveTableRow.setMessageCount(this.messageService.countByDiscussion(d));

                return discussionLiveTableRow;
            }).collect(Collectors.toList()));
        try {
            return Response.ok(new ObjectMapper().writeValueAsString(ltr), MediaType.APPLICATION_JSON).build();
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(String.format(CAUSE_ERROR_RESPONSE, getRootCauseMessage(e))).build();
        }
    }

    @Override
    public Response listusers(String type, String reference, Integer offset, Integer limit,
        Integer reqNo)
    {
        DiscussionReference discussionReference =
            this.discussionReferencesResolver.resolve(reference, DiscussionReference.class);
        LiveTableResult<DiscussionUserRow> ltr = new LiveTableResult<>();
        ltr.setReqNo(reqNo);
        DiscussionsActorService discussionsActorService = this.discussionsActorServiceResolver.get(type);

        Stream<ActorDescriptor> users = discussionsActorService.listUsers(discussionReference);
        Stream<ActorDescriptor> collect = users.sorted(Comparator.comparing(ActorDescriptor::getName));
        if (offset != null) {
            ltr.setOffset(offset);
            collect = collect.skip(offset - 1);
        }
        if (limit != null) {
            collect = collect.limit(limit);
        }
        ltr.setTotalrows(discussionsActorService.countUsers(discussionReference));
        ltr.setRows(collect.map(it -> {
            EntityReference resolve = this.documentReferenceResolver.resolve(it.getLink().toASCIIString());
            String serialize = this.urlSerializer.serialize(resolve);
            return new DiscussionUserRow(it.getName(), serialize);
        }).collect(Collectors.toList()));

        try {
            return Response.ok(new ObjectMapper().writeValueAsString(ltr), MediaType.APPLICATION_JSON).build();
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(String.format(CAUSE_ERROR_RESPONSE, getRootCauseMessage(e))).build();
        }
    }

    @Override
    public Discussion create(CreateDiscussion discussion) throws XWikiRestException
    {
        String applicationHint = discussion.getApplicationHint();
        String title = discussion.getTitle();
        String description = discussion.getDescription();
        String mainDocument = discussion.getMainDocument();
        return this.discussionService.create(applicationHint, title, description, mainDocument)
            .orElseThrow(() -> new XWikiRestException(
                String.format("Fail to create a discussion with title=[%s], description=[%s]", title, description)));
    }
}
