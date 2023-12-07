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

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.store.internal.initializer.DiscussionRedirectXClassInitializer;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.redirection.RedirectionFilter;

/**
 * Custom redirection filter for the dedicated redirect xobject of discussions.
 * This redirection filter only redirects in case of accessing a page with view action.
 *
 * @version $Id$
 * @since 2.5
 */
@Component
@Singleton
@Named(DiscussionRedirectXClassInitializer.SERIALIZED_REFERENCE)
public class DiscussionRedirectionFilter implements RedirectionFilter
{
    @Inject
    private DocumentReferenceResolver<String> resolver;

    @Override
    public boolean redirect(XWikiContext context) throws XWikiException
    {
        if (!StringUtils.equals("view", context.getAction())) {
            return false;
        }

        XWikiDocument doc = context.getDoc();
        BaseObject redirectObj = doc.getXObject(DiscussionRedirectXClassInitializer.XCLASS_REFERENCE);
        if (redirectObj == null) {
            return false;
        }

        // Get the location
        String location = redirectObj.getStringValue(DiscussionRedirectXClassInitializer.LOCATION_FIELD);
        if (StringUtils.isBlank(location)) {
            return false;
        }

        WikiReference wikiReference = context.getWikiReference();

        // Resolve the location to get a reference.
        EntityReference locationReference = this.resolver.resolve(location, wikiReference);

        String url = context.getWiki().getURL(locationReference, context.getAction(),
            context.getRequest().getQueryString(), null, context);

        // Send the redirection
        try {
            context.getResponse().sendRedirect(url);
        } catch (IOException e) {
            throw new XWikiException("Failed to redirect.", e);
        }

        return true;
    }
}
