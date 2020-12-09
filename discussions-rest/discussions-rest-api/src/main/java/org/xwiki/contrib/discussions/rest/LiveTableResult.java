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
package org.xwiki.contrib.discussions.rest;

import java.util.List;

/**
 * Contains the information expected in result of a live table query.
 *
 * @param <T> the type of the lines of the live table
 * @version $Id$
 * @since 1.0
 */
public class LiveTableResult<T>
{
    private long reqNo;

    private long totalrows;

    private List<T> rows;

    private long offset;

    /**
     * @return the number of rows in the current result
     */
    public long getReturnedrows()
    {
        return this.rows.size();
    }

    /**
     * @return the live table request number
     */
    public long getReqNo()
    {
        return this.reqNo;
    }

    /**
     * @param reqNo the live table request number
     */
    public void setReqNo(long reqNo)
    {
        this.reqNo = reqNo;
    }

    /**
     * @return the total number of results of the live table
     */
    public long getTotalrows()
    {
        return this.totalrows;
    }

    /**
     * @param totalrows the total number of results of the live table
     */
    public void setTotalrows(long totalrows)
    {
        this.totalrows = totalrows;
    }

    /**
     * @return the rows of the live table on the current page
     */
    public List<T> getRows()
    {
        return this.rows;
    }

    /**
     * @param rows the total number of results of the live table
     */
    public void setRows(List<T> rows)
    {
        this.rows = rows;
    }

    /**
     * @return the current offset
     */
    public long getOffset()
    {
        return this.offset;
    }

    /**
     * @param offset the current offset
     */
    public void setOffset(long offset)
    {
        this.offset = offset;
    }
}

