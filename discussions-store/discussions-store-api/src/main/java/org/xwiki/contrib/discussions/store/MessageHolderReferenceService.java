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
package org.xwiki.contrib.discussions.store;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.stability.Unstable;

/**
 * Component in charge of creating and manipulating the references of the document that will contain a new message.
 * The idea of this component role is to allow to know in advance where a message will be stored in order to properly
 * manipulate the API that needs this reference: e.g. to resolve relative links, or to temporarily upload attachments.
 * This component is in charge of providing a unique reference associated to both a {@code HttpSession} and a
 * {@link DiscussionReference}: then when a message is created the reference should be declared consumed.
 *
 * @version $Id$
 * @since 2.1
 */
@Unstable
@Role
public interface MessageHolderReferenceService
{
    /**
     * Create or retrieve the message holder reference associated to the given {@link DiscussionReference}.
     * The provided {@link DiscussionStoreConfigurationParameters} is used in case of creation to properly use the
     * space reference configured by the application.
     *
     * @param discussionReference the reference of the discussion for which a new message would be created
     * @param configurationParameters the configuration parameters for knowing where to store the messages
     * @return the reference of the document that will hold the new message once created
     */
    DocumentReference getNextMessageHolderReference(DiscussionReference discussionReference,
        DiscussionStoreConfigurationParameters configurationParameters);

    /**
     * Declare that a document reference has been used to create a message.
     * This method should be called as soon as a message is created with this document so that a new reference can be
     * used.
     *
     * @param discussionReference the reference of the discussion for which a message has been created
     * @param documentReference the reference of the document that holds the new message
     */
    void consumeReference(DiscussionReference discussionReference, DocumentReference documentReference);
}
