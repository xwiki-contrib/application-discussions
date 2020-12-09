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
package org.xwiki.contrib.discussions.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.rest.model.CreateDiscussion;
import org.xwiki.rest.XWikiRestException;
import org.xwiki.stability.Unstable;

/**
 * REST API for the Discussion CRUD.
 *
 * @version $Id$
 * @since 1.0
 */
@Path("/discussions")
@Unstable
public interface DiscussionREST
{
    /**
     * Retrieves a discussion from its reference.
     *
     * @param reference the discussion reference
     * @return the discussion
     * @throws XWikiRestException in case of error when retrieving the discussion
     */
    @Path("/discussion/{reference}")
    @GET
    Discussion get(@PathParam("reference") String reference) throws XWikiRestException;

    /**
     * Returns a list of discussions, paginated and possibly filtered.
     *
     * @param type the type of the discussions
     * @param reference the reference of the discussions
     * @param offset the pagination offset
     * @param limit the pagination limit
     * @param sort name of the column to sort on
     * @param dir the sort direction (asc or desc)
     * @param reqNo the request number
     * @param linkTemplate the template used to generate the links to the discussions
     * @return a paginated list of discussions, in the form of the string of a json object
     */
    @Path("/livetable")
    @GET
    String livetable(@QueryParam("type") String type, @QueryParam("reference") String reference,
        @QueryParam("offset") Integer offset,
        @QueryParam("limit") Integer limit, @QueryParam("sort") String sort, @QueryParam("dir") String dir,
        @QueryParam("reqNo") Integer reqNo, @QueryParam("linkTemplate") String linkTemplate);

    /**
     * Creates a discussion.
     *
     * @param discussion the create discussion object
     * @return the created discussion
     * @throws XWikiRestException in case of error when creating the discussion
     */
    @POST
    @Path("/discussion")
    Discussion create(CreateDiscussion discussion) throws XWikiRestException;
}
