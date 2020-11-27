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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.MessageService;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.domain.Message;
import org.xwiki.contrib.discussions.store.MessageStoreService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_REFERENCE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.AUTHOR_TYPE_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.CONTENT_NAME;
import static org.xwiki.contrib.discussions.store.meta.MessageMetadata.REFERENCE_NAME;

/**
 * Default implementation of {@link MessageService}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultMessageService implements MessageService
{
    private static final String DEFAULT_ACTOR_TYPE = "user";

    @Inject
    private MessageStoreService messageStoreService;

    @Inject
    private DiscussionService discussionService;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @Override
    public Optional<Message> create(String content, Discussion discussion)
    {
        DocumentReference author = this.xcontextProvider.get().getUserReference();
        String authorReference = this.entityReferenceSerializer.serialize(author);

        // TODO check rights before creating
        // TODO checks discussion exists before creating
        return this.discussionService.get(discussion.getReference())
            .flatMap(
                d -> this.messageStoreService.create(content, DEFAULT_ACTOR_TYPE, authorReference, d.getReference())
                    .map(messageReference -> new Message(messageReference, content, DEFAULT_ACTOR_TYPE, authorReference,
                        d)));
    }

    @Override
    public List<Message> getByDiscussion(Discussion discussion, int offset, int limit)
    {
        // TODO rights...
        List<BaseObject> messages = this.messageStoreService.getByDiscussion(discussion.getReference(), offset, limit);
        return messages
            .stream()
            .map(bo -> new Message(
                    bo.getStringValue(REFERENCE_NAME),
                    bo.getLargeStringValue(CONTENT_NAME),
                    bo.getStringValue(AUTHOR_TYPE_NAME),
                    bo.getStringValue(AUTHOR_REFERENCE_NAME),
                    discussion
                )
            ).collect(Collectors.toList());
    }
}
