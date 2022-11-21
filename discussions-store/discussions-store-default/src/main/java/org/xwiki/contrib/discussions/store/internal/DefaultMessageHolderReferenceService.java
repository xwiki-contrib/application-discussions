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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.domain.references.DiscussionReference;
import org.xwiki.contrib.discussions.store.MessageHolderReferenceService;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;

/**
 * Default implementation of {@link MessageHolderReferenceService}.
 *
 * @version $Id$
 * @since 2.1
 */
@Component
@Singleton
public class DefaultMessageHolderReferenceService implements MessageHolderReferenceService
{
    static final String MESSAGE_HOLDER_REFERENCE_SESSION_ATTRIBUTE = "xwikiDiscussionsMessageHolderReferences";

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private PageHolderReferenceFactory pageHolderReferenceFactory;

    private HttpSession getCurrentSession()
    {
        XWikiContext context = this.contextProvider.get();
        return context.getRequest().getHttpServletRequest().getSession();
    }

    private Map<DiscussionReference, DocumentReference> getTemporaryReferencesMapForCurrentSession()
    {
        HttpSession currentSession = getCurrentSession();
        MessageHolderReferenceSession messageHolderReferenceSession =
            (MessageHolderReferenceSession) currentSession.getAttribute(MESSAGE_HOLDER_REFERENCE_SESSION_ATTRIBUTE);
        if (messageHolderReferenceSession == null) {
            messageHolderReferenceSession = new MessageHolderReferenceSession(currentSession.getId());
            currentSession.setAttribute(MESSAGE_HOLDER_REFERENCE_SESSION_ATTRIBUTE, messageHolderReferenceSession);
        }
        return messageHolderReferenceSession.getMessageHolders();
    }

    private DocumentReference createTemporaryMessageReference(DiscussionReference discussionReference,
        DiscussionStoreConfigurationParameters configurationParameters)
    {
        return this.pageHolderReferenceFactory
            .createPageHolderReference(PageHolderReferenceFactory.DiscussionEntity.MESSAGE, "",
                discussionReference.getApplicationHint(), discussionReference, configurationParameters);
    }

    @Override
    public DocumentReference getNextMessageHolderReference(DiscussionReference discussionReference,
        DiscussionStoreConfigurationParameters configurationParameters)
    {
        synchronized (this) {
            Map<DiscussionReference, DocumentReference> temporaryReferencesMapForCurrentSession =
                this.getTemporaryReferencesMapForCurrentSession();
            DocumentReference result;
            if (!temporaryReferencesMapForCurrentSession.containsKey(discussionReference)) {
                result =
                    this.createTemporaryMessageReference(discussionReference, configurationParameters);
                temporaryReferencesMapForCurrentSession.put(discussionReference, result);
            } else {
                result = temporaryReferencesMapForCurrentSession.get(discussionReference);
            }
            return result;
        }
    }

    @Override
    public void consumeReference(DiscussionReference discussionReference, DocumentReference documentReference)
    {
        synchronized (this) {
            Map<DiscussionReference, DocumentReference> temporaryReferencesMapForCurrentSession =
                this.getTemporaryReferencesMapForCurrentSession();

            // We only perform the removal in case of match between the given document reference and the one stored in
            // the map: it might avoid unseen bugs and shouldn't be very expensive.
            if (documentReference.equals(temporaryReferencesMapForCurrentSession.get(discussionReference))) {
                temporaryReferencesMapForCurrentSession.remove(discussionReference);
            }
        }
    }
}
