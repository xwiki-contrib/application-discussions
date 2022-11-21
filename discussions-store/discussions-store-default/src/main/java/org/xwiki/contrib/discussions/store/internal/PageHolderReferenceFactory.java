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

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextEntityReference;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.store.DiscussionStoreConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

/**
 * Utility component in charge of generating unique references for the various entities to store.
 *
 * @version $Id$
 * @since 2.1
 */
@Component(roles = PageHolderReferenceFactory.class)
@Singleton
public class PageHolderReferenceFactory
{
    /**
     * Entities supported for reference creation.
     */
    enum DiscussionEntity
    {
        /**
         * For storing a {@link org.xwiki.contrib.discussions.domain.DiscussionContext}.
         */
        DISCUSSION_CONTEXT,

        /**
         * For storing a {@link org.xwiki.contrib.discussions.domain.Discussion}.
         */
        DISCUSSION,

        /**
         * For storing a {@link org.xwiki.contrib.discussions.domain.Message}.
         */
        MESSAGE
    }

    @Inject
    private DiscussionStoreConfigurationFactory discussionStoreConfigurationFactory;

    /**
     * Generate a unique reference for storing the requested entity at the space location retrieved from the given
     * parameters.
     * <p>
     * The type of the given reference is linked to the requested entity, following this mapping:
     * <ul>
     *     <li>DiscussionContext: the reference should be of type {@link DiscussionContextEntityReference}</li>
     *     <li>Discussion: the reference is not used and can be null</li>
     *     <li>Message: the reference should be of type {@link DiscussionReference}</li>
     * </ul>
     * </p>
     *
     * @param entity the entity to store
     * @param name the name of the entity
     * @param applicationHint the hint of the application used to retrieve the configuration store
     * @param reference the linked reference for storing the entity: the type is related to the entity
     * @param configurationParameters the parameters to use for chosing the storage location
     * @return a unique reference for storing the entity
     */
    public DocumentReference createPageHolderReference(DiscussionEntity entity, String name, String applicationHint,
        Object reference, DiscussionStoreConfigurationParameters configurationParameters)
    {
        String generatedString = UUID.randomUUID().toString();
        DiscussionStoreConfiguration discussionStoreConfiguration =
            this.discussionStoreConfigurationFactory.getDiscussionStoreConfiguration(applicationHint);

        SpaceReference spaceLocation;
        switch (entity) {
            case DISCUSSION_CONTEXT:
                spaceLocation = discussionStoreConfiguration.getDiscussionContextSpaceStorageLocation(
                    configurationParameters, (DiscussionContextEntityReference) reference);
                break;

            case DISCUSSION:
                spaceLocation = discussionStoreConfiguration.getDiscussionSpaceStorageLocation(configurationParameters);
                break;

            case MESSAGE:
                spaceLocation = discussionStoreConfiguration.getMessageSpaceStorageLocation(configurationParameters,
                    (DiscussionReference) reference);
                break;

            default:
                throw new IllegalArgumentException(
                    String.format("The provided entity [%s] is not correct for this method.", entity));
        }
        String pageTitle;
        if (StringUtils.isBlank(name)) {
            pageTitle = generatedString;
        } else {
            pageTitle = String.format("%s-%s", name, generatedString);
        }
        return new DocumentReference(pageTitle, spaceLocation);
    }
}
