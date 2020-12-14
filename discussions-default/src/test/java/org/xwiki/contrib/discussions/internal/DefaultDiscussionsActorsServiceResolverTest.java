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

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.discussions.DiscussionsActorService;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test of {@link DefaultDiscussionsActorsServiceResolver}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class DefaultDiscussionsActorsServiceResolverTest
{
    @InjectMockComponents
    private DefaultDiscussionsActorsServiceResolver defaultDiscussionsActorsServiceResolver;

    @MockComponent
    private ComponentManager componentManager;

    @MockComponent
    private DiscussionsActorService defaultDiscussionsActorService;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.DEBUG);

    @Test
    void get() throws Exception
    {
        DiscussionsActorService expected = mock(DiscussionsActorService.class);
        when(this.componentManager.getInstance(DiscussionsActorService.class, "knownType")).thenReturn(
            expected);
        Optional<DiscussionsActorService> actual = this.defaultDiscussionsActorsServiceResolver.get("knownType");
        assertEquals(Optional.of(expected), actual);
    }

    @Test
    void getUnknown() throws Exception
    {
        when(this.componentManager.getInstance(DiscussionsActorService.class, "unknownType"))
            .thenThrow(ComponentLookupException.class);
        Optional<DiscussionsActorService> actual = this.defaultDiscussionsActorsServiceResolver.get("unknownType");
        assertEquals(Optional.of(this.defaultDiscussionsActorService), actual);
        assertEquals(1, this.logCapture.size());
        assertEquals("Failed to find an actor service for type [unknownType]. Cause: [ComponentLookupException: ]",
            this.logCapture.getMessage(0));
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(0).getLevel());
    }
}