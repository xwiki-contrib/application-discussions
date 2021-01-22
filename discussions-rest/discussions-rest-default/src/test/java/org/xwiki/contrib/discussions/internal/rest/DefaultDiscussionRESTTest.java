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
package org.xwiki.contrib.discussions.internal.rest;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.contrib.discussions.DiscussionService;
import org.xwiki.contrib.discussions.DiscussionsActorService;
import org.xwiki.contrib.discussions.DiscussionsActorServiceResolver;
import org.xwiki.contrib.discussions.MessageService;
import org.xwiki.contrib.discussions.domain.ActorDescriptor;
import org.xwiki.contrib.discussions.domain.Discussion;
import org.xwiki.contrib.discussions.rest.DiscussionUserRow;
import org.xwiki.contrib.discussions.rest.model.CreateDiscussion;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rest.XWikiRestException;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test of {@link DefaultDiscussionREST}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class DefaultDiscussionRESTTest
{
    @InjectMockComponents
    private DefaultDiscussionREST target;

    @MockComponent
    private DiscussionService discussionService;

    @MockComponent
    private MessageService messageService;

    @MockComponent
    private DiscussionsActorServiceResolver discussionsActorServiceResolver;

    @MockComponent
    @Named("url")
    private EntityReferenceSerializer<String> urlSerializer;

    @MockComponent
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Test
    void get() throws Exception
    {
        Discussion discussion = new Discussion("r", "ttl", "desc", new Date(), null);
        when(this.discussionService.get("ref")).thenReturn(Optional.of(discussion));
        Discussion actual = this.target.get("ref");
        assertEquals(discussion, actual);
    }

    @Test
    void getNotFound()
    {
        when(this.discussionService.get("ref")).thenReturn(Optional.empty());
        Throwable ref = assertThrows(XWikiRestException.class, () -> this.target.get("ref"));
        assertEquals("Discussion with reference=[ref] not found.", ref.getMessage());
    }

    @ParameterizedTest
    @CsvSource(value = { "true;{\"reqNo\":1,\"totalrows\":123,\"rows\":[{\"title\":\"d1-ttl\","
        + "\"updateDate\":\"2020/06/03 "
        + "04:01\",\"messageCount\":5,\"title_url\":\"http://server/ref=__AAA__\",\"doc_viewable\":true},"
        + "{\"title\":\"d2-ttl\",\"updateDate\":\"2020/06/03 04:01\",\"messageCount\":15,"
        + "\"title_url\":\"http://server/ref=__AAA__\",\"doc_viewable\":true},{\"title\":\"d3-ttl\","
        + "\"updateDate\":\"2020/06/03 04:01\",\"messageCount\":0,\"title_url\":\"http://server/ref=__AAA__\","
        + "\"doc_viewable\":true}],\"offset\":1,\"returnedrows\":3}",
        "false;{\"reqNo\":1,\"totalrows\":12,\"rows\":[{\"title\":\"d1-ttl\",\"updateDate\":\"2020/06/03 04:01\","
            + "\"messageCount\":5,\"title_url\":\"http://server/ref=__AAA__\",\"doc_viewable\":true},"
            + "{\"title\":\"d2-ttl\",\"updateDate\":\"2020/06/03 04:01\",\"messageCount\":15,"
            + "\"title_url\":\"http://server/ref=__AAA__\",\"doc_viewable\":true}],\"offset\":1,"
            + "\"returnedrows\":2}" }, delimiter = ';')
    void livetable(boolean joker, String expected)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JUNE, 3, 4, 1, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC+1"));
        when(this.discussionService.countByEntityReferences("test-type", asList("test-ref"))).thenReturn(12L);
        when(this.discussionService.countByEntityReferences("test-type", asList("test-ref", "*"))).thenReturn(123L);
        Discussion discussion1 = new Discussion("d1-ref", "d1-ttl", "d1-desc", calendar.getTime(), null);
        Discussion discussion2 = new Discussion("d2-ref", "d2-ttl", "d2-desc", calendar.getTime(), null);
        Discussion discussion3 = new Discussion("d3-ref", "d3-ttl", "d3-desc", calendar.getTime(), null);
        when(this.discussionService.findByEntityReferences("test-type", asList("test-ref"), 0, 10))
            .thenReturn(asList(discussion1, discussion2));
        when(this.discussionService.findByEntityReferences("test-type", asList("test-ref", "*"), 0, 10))
            .thenReturn(asList(discussion1, discussion2, discussion3));
        when(this.messageService.countByDiscussion(discussion1)).thenReturn(5L);
        when(this.messageService.countByDiscussion(discussion2)).thenReturn(15L);
        Response response = this.target.livetable("test-type", "test-ref", 1, 10, "col", "asc", 1, "http://server/ref"
                + "=__AAA__",
            joker);
        assertEquals(200, response.getStatus());
        assertEquals(expected, response.getEntity());
    }

    @Test
    void listusers()
    {
        DiscussionsActorService discussionsActorService = mock(DiscussionsActorService.class);
        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "U1");
        ActorDescriptor actorDescriptor = new ActorDescriptor();
        actorDescriptor.setName("testname");
        actorDescriptor.setLink(URI.create("xwiki:XWiki.U1"));

        when(this.discussionsActorServiceResolver.get("testtype")).thenReturn(Optional.of(discussionsActorService));
        when(discussionsActorService.listUsers("testreference")).thenReturn(Stream.of(
            actorDescriptor
        ));
        when(discussionsActorService.countUsers("testreference")).thenReturn(1L);
        when(this.documentReferenceResolver.resolve("xwiki:XWiki.U1"))
            .thenReturn(documentReference);
        when(this.urlSerializer.serialize(documentReference)).thenReturn("https://xwiki.org/U1");

        Response response = this.target.listusers("testtype", "testreference", null, null, 1);
        assertEquals(200, response.getStatus());
        String expected =
            "{\"reqNo\":1,\"totalrows\":1,\"rows\":[{\"name\":\"testname\",\"name_url\":\"https://xwiki.org/U1\","
                + "\"doc_viewable\":true}],\"offset\":0,\"returnedrows\":1}";
        assertEquals(expected, response.getEntity());
    }

    @Test
    void create() throws Exception
    {
        CreateDiscussion createDiscussion = new CreateDiscussion()
            .setTitle("title")
            .setDescription("description")
            .setMainDocument("XWiki.Doc");
        Discussion discussion = new Discussion();
        when(this.discussionService.create("title", "description", "XWiki.Doc")).thenReturn(Optional.of(
            discussion));
        Discussion actual = this.target.create(createDiscussion);
        assertSame(discussion, actual);
    }

    @Test
    void createException()
    {
        CreateDiscussion createDiscussion = new CreateDiscussion()
            .setTitle("title")
            .setDescription("description")
            .setMainDocument("XWiki.Doc");
        when(this.discussionService.create("title", "description", "XWiki.Doc")).thenReturn(Optional.empty());
        XWikiRestException throwable =
            assertThrows(XWikiRestException.class, () -> this.target.create(createDiscussion));
        assertEquals("Fail to create a discussion with title=[title], description=[description]",
            throwable.getMessage());
    }
}