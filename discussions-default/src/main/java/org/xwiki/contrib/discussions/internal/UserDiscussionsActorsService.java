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

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionContextService;
import org.xwiki.contrib.discussions.DiscussionsActorService;
import org.xwiki.contrib.discussions.domain.ActorDescriptor;
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

/**
 * Resolve an actor descriptor from a local wiki user reference.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("user")
@Singleton
public class UserDiscussionsActorsService implements DiscussionsActorService
{
    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private DiscussionContextService discussionContextService;

    @Override
    public Optional<ActorDescriptor> resolve(String reference)
    {
        return Optional.of(internalResolve(reference));
    }

    /**
     * Same as {@link #resolve(String)}, without the wrapping in an {@link Optional}.
     */
    private ActorDescriptor internalResolve(String reference)
    {
        DocumentReference userDocumentReference = this.documentReferenceResolver.resolve(reference);
        ActorDescriptor actorDescriptor = new ActorDescriptor();
        XWikiContext context = this.xcontextProvider.get();
        XWiki wiki = context.getWiki();
        actorDescriptor.setName(wiki.getPlainUserName(userDocumentReference, context));
        actorDescriptor.setLink(URI.create(reference));
        return actorDescriptor;
    }

    @Override
    public Stream<ActorDescriptor> listUsers(String discussionReference)
    {
        return discussionUserStream(discussionReference)
            .map(it -> internalResolve(it.getEntityReference().getReference()));
    }

    @Override
    public long countUsers(String discussionReference)
    {
        return discussionUserStream(discussionReference).count();
    }

    private Stream<DiscussionContext> discussionUserStream(String discussionReference)
    {
        return this.discussionContextService.findByDiscussionReference(discussionReference)
            .stream()
            .filter(it -> it.getEntityReference().getType().equals("messagestream-user"))
            .filter(it -> !it.getEntityReference().getType().equals("*"));
    }
}
