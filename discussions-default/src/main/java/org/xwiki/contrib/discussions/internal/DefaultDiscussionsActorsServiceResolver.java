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
package org.xwiki.contrib.discussions.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.discussions.DiscussionsActorService;
import org.xwiki.contrib.discussions.DiscussionsActorServiceResolver;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Resolve the actor services according to the request type.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultDiscussionsActorsServiceResolver implements DiscussionsActorServiceResolver
{
    @Inject
    @Named("context")
    private ComponentManager componentManager;

    @Inject
    private DiscussionsActorService defaultDiscussionsActorService;

    @Inject
    private Logger logger;

    @Override
    public DiscussionsActorService get(String type)
    {
        DiscussionsActorService result = this.defaultDiscussionsActorService;
        try {
            if (this.componentManager.hasComponent(DiscussionsActorService.class, type)) {
                result = this.componentManager.getInstance(DiscussionsActorService.class, type);
            }
        } catch (ComponentLookupException e) {
            this.logger
                .warn("Error to initialize service for type [{}]. Cause: [{}]", type, getRootCauseMessage(e));
        }
        return result;
    }
}
