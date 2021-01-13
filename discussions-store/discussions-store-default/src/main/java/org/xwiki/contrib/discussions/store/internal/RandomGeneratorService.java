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

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import static org.apache.commons.lang3.RandomStringUtils.random;

/**
 * Provides operations to get random values.
 *
 * @version $Id$
 * @since 1.0
 */
@Component(roles = { RandomGeneratorService.class })
@Singleton
public class RandomGeneratorService
{
    /**
     * @return a randomly generated string of 6 characters
     */
    public String randomString()
    {
        int length = 6;
        return randomString(length);
    }

    /**
     * @param length the random string length
     * @return a randomly generated string of arbitrary length
     */
    public String randomString(int length)
    {
        boolean useLetters = true;
        boolean useNumbers = true;
        return random(length, useLetters, useNumbers);
    }
}
