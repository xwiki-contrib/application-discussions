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
package org.xwiki.contrib.discussions.store.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.discussions.store.DiscussionStoreConfiguration;

/**
 * Factory that allows to retrieve a {@link DiscussionStoreConfiguration} based on an application hint, or to fallback
 * to the {@link DefaultDiscussionStoreConfiguration} in case it doesn't exist.
 *
 * @version $Id$
 * @since 2.0
 */
@Component(roles = DiscussionStoreConfigurationFactory.class)
@Singleton
public class DiscussionStoreConfigurationFactory
{
    @Inject
    @Named("context")
    private ComponentManager componentManager;

    @Inject
    private DiscussionStoreConfiguration defaultDiscussionStoreConfiguration;

    @Inject
    private Logger logger;

    /**
     * Retrieve a {@link DiscussionStoreConfiguration} based on the given application hint.
     * @param applicationHint the hint for which to find a {@link DiscussionStoreConfiguration}.
     * @return the {@link DiscussionStoreConfiguration} corresponding to the hint, or the
     *         {@link DefaultDiscussionStoreConfiguration} if there's no implementation found with this hint.
     */
    public DiscussionStoreConfiguration getDiscussionStoreConfiguration(String applicationHint)
    {
        DiscussionStoreConfiguration discussionStoreConfiguration = this.defaultDiscussionStoreConfiguration;
        if (this.componentManager.hasComponent(DiscussionStoreConfiguration.class, applicationHint)) {
            try {
                discussionStoreConfiguration =
                    this.componentManager.getInstance(DiscussionStoreConfiguration.class, applicationHint);
            } catch (ComponentLookupException e) {
                this.logger.warn("Error while trying to load DiscussionStoreConfiguration with hint [{}]: [{}]",
                    applicationHint, ExceptionUtils.getRootCauseMessage(e));
            }
        }
        return discussionStoreConfiguration;
    }
}
