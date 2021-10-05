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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.internal.AbstractDiscussionStoreConfiguration;
import org.xwiki.model.reference.SpaceReference;

import com.xpn.xwiki.XWikiContext;

/**
 * Dedicated {@link org.xwiki.contrib.discussions.store.DiscussionStoreConfiguration} for the message stream
 * application.
 *
 * @version $Id$
 * @since 2.0
 */
@Component
@Named(DiscussionMessageStreamConfiguration.DISCUSSION_MESSAGESTREAM_HINT)
@Singleton
public class DiscussionMessageStreamConfiguration extends AbstractDiscussionStoreConfiguration
{
    /**
     * Application hint to be used for message stream discussions.
     */
    public static final String DISCUSSION_MESSAGESTREAM_HINT = "messagestream";

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override
    public SpaceReference getRootSpaceStorageLocation()
    {
        return new SpaceReference("MessageStream", this.contextProvider.get().getWikiReference());
    }
}
