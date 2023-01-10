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

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.contrib.discussions.DiscussionReferencesSerializer;
import org.xwiki.contrib.discussions.domain.references.DiscussionContextReference;
import org.xwiki.contrib.discussions.store.meta.DiscussionContextMetadata;
import org.xwiki.contrib.discussions.store.meta.DiscussionMetadata;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.query.Query.XWQL;

/**
 * Abstract implementation for components needing to find discussion context.
 *
 * @version $Id$
 * @since 2.2
 */
public abstract class AbstractDiscussionContextStore
{
    @Inject
    private Logger logger;

    @Inject
    private DiscussionContextMetadata discussionContextMetadata;

    @Inject
    private QueryManager queryManager;

    @Inject
    private DiscussionReferencesSerializer discussionReferencesSerializer;

    protected Optional<String> findDiscussionContextPage(DiscussionContextReference reference)
    {
        try {
            String discussionClass = this.discussionContextMetadata.getDiscussionContextXClassFullName();
            List<String> execute =
                this.queryManager
                    .createQuery(
                        String.format("FROM doc.object(%s) obj where obj.%s = :%s",
                            discussionClass,
                            DiscussionMetadata.REFERENCE_NAME,
                            DiscussionMetadata.REFERENCE_NAME),
                        XWQL)
                    .bindValue(DiscussionMetadata.REFERENCE_NAME,
                        this.discussionReferencesSerializer.serialize(reference))
                    .execute();
            if (execute == null || execute.isEmpty()) {
                return Optional.empty();
            }
            if (execute.size() > 1) {
                this.logger.debug("More than one discussion found for reference=[{}]", reference);
            }
            return Optional.of(execute.get(0));
        } catch (QueryException e) {
            this.logger.warn("Failed to get the Discussion with reference=[{}]. Cause: [{}]", reference,
                getRootCauseMessage(e));
            return Optional.empty();
        }
    }
}
