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
package org.xwiki.contrib.discussions.store.internal.initializer;

import java.util.Objects;

import javax.inject.Inject;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.doc.MandatoryDocumentInitializer;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Common operations for the discussions XClass initializers.
 *
 * @version $Id$
 * @since 1.1
 */
public abstract class AbstractDiscussionContextXClassInitializer implements MandatoryDocumentInitializer
{
    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    private DocumentReference superadmin;

    protected boolean initCreatorReference(XWikiDocument document)
    {
        boolean needsUpdate;
        if (!Objects.equals(document.getCreatorReference(), this.getSuperAdmin())) {
            document.setCreatorReference(this.getSuperAdmin());
            needsUpdate = true;
        } else {
            needsUpdate = false;
        }
        return needsUpdate;
    }

    protected boolean initAuthorReference(XWikiDocument document)
    {
        boolean needsUpdate;
        if (!Objects.equals(document.getAuthorReference(), this.getSuperAdmin())) {
            document.setAuthorReference(this.getSuperAdmin());
            needsUpdate = true;
        } else {
            needsUpdate = false;
        }
        return needsUpdate;
    }

    private DocumentReference getSuperAdmin()
    {
        if (this.superadmin == null) {
            this.superadmin =
                new DocumentReference(this.wikiDescriptorManager.getMainWikiId(), XWiki.SYSTEM_SPACE, "superadmin");
        }
        return this.superadmin;
    }
}
