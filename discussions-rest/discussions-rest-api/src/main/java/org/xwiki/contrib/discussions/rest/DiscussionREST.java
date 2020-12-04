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
@Path("/discussions/discussion")
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
    @Path("{reference}")
    @GET
    Discussion get(@PathParam("reference") String reference) throws XWikiRestException;

    /**
     * Creates a discussion.
     *
     * @param discussion the create discussion object
     * @return the created discussion
     * @throws XWikiRestException in case of error when creating the discussion
     */
    @POST
    Discussion create(CreateDiscussion discussion) throws XWikiRestException;
}
