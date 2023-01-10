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
import org.xwiki.contrib.discussions.domain.DiscussionContext;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Interface dedicated for saving context metadata.
 * We separate this interface from {@link DiscussionContextStoreService} as the metadata might be kept in separate
 * objects.
 *
 * @version $Id$
 * @since 2.2
 */
@Role
public interface DiscussionContextMetadataStoreService
{
    /**
     * Read the metadata for the given discussion context.
     * This method will modify the given discussion context to set the metadata values.
     *
     * @param discussionContext the context for which to read the metadata.
     * @return {@code true} if some metadata has been found and the discussion context has been modified.
     */
    boolean readMetadata(DiscussionContext discussionContext);

    /**
     * Read the metadata for the given discussion context from the given reference.
     * This method will modify the given discussion context to set the metadata values.
     *
     * @param discussionContextPage the reference where to read metadata from.
     * @param discussionContext the context for which to read the metadata.
     * @return {@code true} if some metadata has been found and the discussion context has been modified.
     */
    boolean readMetadata(DocumentReference discussionContextPage, DiscussionContext discussionContext);

    /**
     * Read the metadata for the given discussion context from the given document.
     * This method will modify the given discussion context to set the metadata values.
     *
     * @param discussionContextDocument the document where to read metadata from.
     * @param discussionContext the context for which to read the metadata.
     * @return {@code true} if some metadata has been found and the discussion context has been modified.
     */
    boolean readMetadata(XWikiDocument discussionContextDocument, DiscussionContext discussionContext);

    /**
     * Save a new metadata to be added in the discussion context.
     * This method both puts the metadata in {@link DiscussionContext#getMetadata()} map and also persists it
     * permanently.
     *
     * @param discussionContext the context for which to add the new metadata.
     * @param key the key of the metadata to save
     * @param value the value of the metadata
     * @return {@code true} if the metadata was properly saved.
     */
    boolean saveMetadata(DiscussionContext discussionContext, String key, String value);
}
