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
package org.xwiki.contrib.discussions.server;

import org.xwiki.stability.Unstable;

/**
 * Exceptions thrown in case of problem when using server API.
 *
 * @version $Id$
 * @since 2.1
 */
@Unstable
public class DiscussionServerException extends Exception
{
    private final int statusCode;

    /**
     * Default constructor to use when there's no parent exception.
     *
     * @param statusCode the http status code that should be answered
     * @param msg the message of the exception
     */
    public DiscussionServerException(int statusCode, String msg)
    {
        this(statusCode, msg, null);
    }

    /**
     * Default constructor to use when there's a parent exception.
     *
     * @param statusCode the http status code that should be answered
     * @param msg the message of the exception
     * @param cause the original error
     */
    public DiscussionServerException(int statusCode, String msg, Throwable cause)
    {
        super(msg, cause);
        this.statusCode = statusCode;
    }

    /**
     * @return the http status code that should be answered
     */
    public int getStatusCode()
    {
        return this.statusCode;
    }
}
