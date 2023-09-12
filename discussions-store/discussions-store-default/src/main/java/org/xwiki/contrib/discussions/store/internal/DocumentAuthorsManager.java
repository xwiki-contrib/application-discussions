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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.references.ActorReference;
import org.xwiki.model.document.DocumentAuthors;
import org.xwiki.user.GuestUserReference;
import org.xwiki.user.UserReference;
import org.xwiki.user.UserReferenceResolver;

/**
 * A utility component for setting the authors of discussion pages based on the parameters.
 *
 * @version $Id$
 * @since 2.4
 */
@Component(roles = DocumentAuthorsManager.class)
@Singleton
public class DocumentAuthorsManager
{
    @Inject
    private UserReferenceResolver<String> stringUserReferenceResolver;

    /**
     * Set the authors based on the parameters and on the actor reference.
     * This method doesn't return anything but modifies the given {@link DocumentAuthors}.
     * @param documentAuthors the authors to be set
     * @param actorReference an actor reference to use as fallback if no parameter is given, this parameter can be
     *                      {@code null}
     * @param parameters the parameters to look for the author information
     */
    public void setDocumentAuthors(DocumentAuthors documentAuthors, ActorReference actorReference,
        DiscussionStoreConfigurationParameters parameters)
    {
        if (documentAuthors.getCreator() == null || GuestUserReference.INSTANCE.equals(documentAuthors.getCreator())) {
            getReference(actorReference, parameters, DiscussionStoreConfigurationParameters.CREATOR_PARAMETER_KEY)
                .ifPresent(documentAuthors::setCreator);
        }
        getReference(actorReference, parameters, DiscussionStoreConfigurationParameters.EFFECTIVE_AUTHOR_PARAMETER_KEY)
            .ifPresent(documentAuthors::setEffectiveMetadataAuthor);
        getReference(actorReference, parameters, DiscussionStoreConfigurationParameters.ORIGINAL_AUTHOR_PARAMETER_KEY)
            .ifPresent(documentAuthors::setOriginalMetadataAuthor);
    }

    private Optional<UserReference> getReference(ActorReference actorReference,
        DiscussionStoreConfigurationParameters parameters, String key)
    {
        Optional<UserReference> result = Optional.empty();
        if (parameters.containsKey(key) && parameters.get(key) instanceof UserReference) {
            result = Optional.of((UserReference) parameters.get(key));
        } else if (actorReference != null && StringUtils.equals(actorReference.getType(), "user")) {
            result = Optional.of(this.stringUserReferenceResolver.resolve(actorReference.getReference()));
        }
        return result;
    }
}
