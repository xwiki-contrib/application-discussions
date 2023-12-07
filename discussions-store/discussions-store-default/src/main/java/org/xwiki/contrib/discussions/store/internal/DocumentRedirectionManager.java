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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionStoreConfigurationParameters;
import org.xwiki.contrib.discussions.store.internal.initializer.DiscussionRedirectXClassInitializer;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Component in charge of creating a redirection xobject when specified by the parameters.
 *
 * @version $Id$
 * @since 2.3
 */
@Component(roles = DocumentRedirectionManager.class)
@Singleton
public class DocumentRedirectionManager
{
    private static final String REDIRECTION_PARAMETER = "redirection";

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @Inject
    private Logger logger;

    /**
     * Creates a new redirect xobject in the provided document if configuration contains a redirection parameters.
     * Note that this method does not perform a save of the modified document.
     *
     * @param document the document for which to add a redirect xobject if requested
     * @param configurationParameters the parameters where the redirection might be defined
     */
    public void handleCreatingRedirection(XWikiDocument document,
        DiscussionStoreConfigurationParameters configurationParameters)
    {
        if (configurationParameters.containsKey(REDIRECTION_PARAMETER)) {
            Object redirectionObject = configurationParameters.get(REDIRECTION_PARAMETER);
            String redirection = null;
            if (redirectionObject instanceof String) {
                redirection = (String) redirectionObject;
            } else if (redirectionObject instanceof String[]) {
                redirection = ((String[]) redirectionObject)[0];
            } else if (redirectionObject instanceof EntityReference) {
                redirection = this.entityReferenceSerializer.serialize((EntityReference) redirectionObject);
            }

            if (!StringUtils.isBlank(redirection)) {
                try {
                    BaseObject redirectXObject =
                        document.newXObject(DiscussionRedirectXClassInitializer.XCLASS_REFERENCE,
                            this.contextProvider.get());
                    redirectXObject.setStringValue("location", redirection);
                } catch (XWikiException e) {
                    this.logger.error("Error while trying to create redirection xobject in [{}]: [{}]",
                        document.getDocumentReference(), ExceptionUtils.getRootCauseMessage(e));
                    this.logger.debug("Full stack trace: ", e);
                }
            }
        }
    }
}
