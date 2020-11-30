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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Service providing operations to manipulate and serialize query strings.
 *
 * @version $Id$
 * @since 1.0
 */
@Component(roles = { QueryStringService.class })
@Singleton
public class QueryStringService
{
    private static final String QUERY_STRING_EQ = "=";

    private static final String QUERY_STRING_SEPARATOR = "&";

    /**
     * Merge newParameterMap on top of parameterMap and serialize the result as query string.
     *
     * @param parameterMap the parameter map
     * @param newParameterMap new values for the parameter map
     * @return the resulting query string
     */
    public String getString(Map<String, Object> parameterMap, Map<String, Object> newParameterMap)
    {
        Map<Object, Object> stringHashMap;
        if (parameterMap != null) {
            stringHashMap = new HashMap<>(parameterMap);
        } else {
            stringHashMap = new HashMap<>();
        }
        if (newParameterMap != null) {
            stringHashMap.putAll(newParameterMap);
        }

        // TODO: simplify!
        return stringHashMap.entrySet()
            .stream()
            .flatMap(it -> {
                Object value = it.getKey();
                try {
                    String enc = StandardCharsets.UTF_8.toString();
                    String key = URLEncoder.encode(String.valueOf(value), enc);

                    if (it.getValue() instanceof List) {
                        return (Stream<String>) urlEncodeStream(key, ((List) it.getValue()).stream());
                    } else if (it.getClass().isArray()) {
                        return urlEncodeStream(key, Arrays.stream((Object[]) it.getValue()));
                    } else {
                        return Stream.of(key + QUERY_STRING_EQ + URLEncoder.encode(String.valueOf(it.getValue()), enc));
                    }
                } catch (UnsupportedEncodingException e) {
                    return Stream.of();
                }
            })
            .collect(Collectors.joining(QUERY_STRING_SEPARATOR));
    }

    private Stream<String> urlEncodeStream(String key, Stream<Object> values)
    {
        String enc = StandardCharsets.UTF_8.toString();
        return values.map(it2 -> {
            try {
                return key + QUERY_STRING_EQ + URLEncoder.encode(String.valueOf(it2), enc);
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        });
    }
}
