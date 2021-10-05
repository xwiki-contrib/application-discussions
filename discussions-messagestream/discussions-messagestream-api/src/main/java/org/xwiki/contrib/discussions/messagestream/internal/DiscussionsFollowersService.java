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
package org.xwiki.contrib.discussions.messagestream.internal;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * This service provides operations to find the followers of a user.
 *
 * @version $Id$
 * @since 1.0
 */
@Component(roles = { DiscussionsFollowersService.class })
@Singleton
public class DiscussionsFollowersService
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    /**
     * Returns the list of followers of a user.
     * <p>
     * Once XWIKI-18216 is closed, this component must be removed and replaced by the right new API call.
     *
     * @param followed the follower user
     * @return the list of users the follow the followed user
     */
    public List<String> getFollowers(String followed)
    {
        try {
            return this.queryManager.createQuery("select nfp.owner from DefaultNotificationFilterPreference nfp "
                + "where nfp.active=true "
                + "and nfp.enabled=true "
                + "and nfp.filterName='eventUserNotificationFilter' "
                + "and user=:followed"
                + " ", Query.HQL)
                .bindValue("followed", followed).execute();
        } catch (QueryException e) {
            this.logger.warn("Failed to get the followers of [{}]. Cause: [{}].", followed, getRootCauseMessage(e));
            return Arrays.asList();
        }
    }
}
