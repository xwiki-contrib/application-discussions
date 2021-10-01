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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.discussions.DiscussionReferencesResolver;
import org.xwiki.contrib.discussions.domain.references.AbstractDiscussionReference;

/**
 * Default implementation of {@link DiscussionReferencesResolver} based on the serialization defined in
 * {@link DefaultDiscussionReferencesSerializer}.
 *
 * @see DefaultDiscussionReferencesSerializer
 * @version $Id$
 * @since 2.0
 */
@Component
@Singleton
public class DefaultDiscussionReferencesResolver implements DiscussionReferencesResolver
{
    private static final String REFERENCE_PATTERN_REGEX = "^(?<reference>.+);applicationHint=(?<applicationHint>.+)$";
    private static final Pattern REFERENCE_PATTERN = Pattern.compile(REFERENCE_PATTERN_REGEX);

    @Inject
    private Logger logger;

    @Override
    public <T extends AbstractDiscussionReference> T resolve(String serializedReference, Class<T> type)
    {
        Matcher matcher = REFERENCE_PATTERN.matcher(serializedReference);
        String applicationHint = "";
        String reference = serializedReference;
        if (matcher.matches()) {
            applicationHint = matcher.group("applicationHint");
            reference = matcher.group("reference");
        }

        return build(type, applicationHint, reference);
    }

    private <T extends AbstractDiscussionReference> T build(Class<T> type, String applicationHint, String reference)
    {
        Constructor<T> constructor = null;
        try {
            constructor = type.getConstructor(String.class, String.class);
            return constructor.newInstance(applicationHint, reference);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            logger.error("Error while trying to instantiate [{}] with application hint [{}] and reference [{}]: [{}]",
                type.getTypeName(), applicationHint, reference, ExceptionUtils.getRootCauseMessage(e));
        }
        return null;
    }
}
