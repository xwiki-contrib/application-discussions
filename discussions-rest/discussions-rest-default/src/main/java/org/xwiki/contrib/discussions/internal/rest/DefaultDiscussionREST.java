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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.rest.DiscussionLiveTableRow;
import org.xwiki.contrib.discussions.rest.DiscussionREST;
import org.xwiki.contrib.discussions.rest.LiveTableResult;
import org.xwiki.contrib.discussions.rest.model.CreateDiscussion;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.XWikiRestException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public String livetable(String type, String reference, Integer offset, Integer limit, String sort, String dir,
        Integer reqNo, String linkTemplate)
    {
        // TODO: deal with missing type
        // TODO: add sort
        LiveTableResult<DiscussionLiveTableRow> ltr = new LiveTableResult<>();
        ltr.setOffset(offset);
        ltr.setReqNo(reqNo);
        ltr.setTotalrows(this.discussionService.countByEntityReference(type, reference));
        ltr.setRows(this.discussionService.findByEntityReference(type, reference, offset, limit)
            .stream()
            .map(d -> {
                DiscussionLiveTableRow discussionLiveTableRow = new DiscussionLiveTableRow();
                discussionLiveTableRow.setTitle(d.getTitle());
                try {
                    discussionLiveTableRow.setTitleUrl(linkTemplate.replace("__REFERENCE__", URLEncoder
                        .encode(d.getReference(), "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    // TODO
                }
                discussionLiveTableRow.setUpdateDate(d.getUpdateDate());
                return discussionLiveTableRow;
            }).collect(Collectors.toList()));
        try {
            return new ObjectMapper().writeValueAsString(ltr);
        } catch (JsonProcessingException e) {
            // TODO: better logging and error handling
            return "{'error': 'failed to serialize'}";
        }
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
