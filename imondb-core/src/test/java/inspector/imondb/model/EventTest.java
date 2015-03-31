package inspector.imondb.model;

/*
 * #%L
 * iMonDB Core
 * %%
 * Copyright (C) 2014 - 2015 InSPECtor
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;

public class EventTest {

    private final Instrument instrument = new Instrument("instrument", InstrumentModel.UNKNOWN_MODEL, new CV("label", "name", "uri", "version"));

    @Test(expected = NullPointerException.class)
    public void setInstrument_null() {
        new Event(null, new Timestamp(new Date().getTime()), EventType.UNDEFINED);
    }

    @Test(expected = NullPointerException.class)
    public void setDate_null() {
        new Event(instrument, null, EventType.UNDEFINED);
    }

    @Test(expected = NullPointerException.class)
    public void setType() {
        new Event(instrument, new Timestamp(new Date().getTime()), null);
    }

    @Test
    public void setAttachment() {
        Event event = new Event(instrument, new Timestamp(new Date().getTime()), EventType.UNDEFINED);
        File attachment = loadResource("/attachment.jpg");
        event.setAttachment(attachment);

        assertTrue(event.getAttachmentName().equals(attachment.getName()));
        try {
            assertArrayEquals(event.getAttachmentContent(), FileUtils.readFileToByteArray(attachment));
        } catch(IOException e) {
            fail();
        }
    }
    @Test(expected = IllegalArgumentException.class)
    public void setAttachment_invalid() {
        Event event = new Event(instrument, new Timestamp(new Date().getTime()), EventType.UNDEFINED);

        try {
            event.setAttachment(new File("invalid.jpg"));
        } finally {
            assertNull(event.getAttachmentName());
            assertNull(event.getAttachmentContent());
        }
    }

    @Test
    public void setAttachment_null() {
        Event event = new Event(instrument, new Timestamp(new Date().getTime()), EventType.UNDEFINED);
        File attachment = loadResource("/attachment.jpg");

        // set null file attachment
        event.setAttachment(attachment);
        event.setAttachment(null);

        assertNull(event.getAttachmentName());
        assertNull(event.getAttachmentContent());

        // set null attachment elements
        event.setAttachment(attachment);
        event.setAttachmentName(null);
        assertNull(event.getAttachmentName());
        event.setAttachmentContent(null);
        assertNull(event.getAttachmentContent());
    }

    @Test
    public void equals_null() {
        Timestamp date = new Timestamp(new Date().getTime());
        Event event = new Event(instrument, date, EventType.UNDEFINED);
        Instrument instrument2 = new Instrument("other instrument", InstrumentModel.UNKNOWN_MODEL, new CV("label", "name", "uri", "version"));
        Event eventInstrument = new Event(instrument2, date, EventType.UNDEFINED);
        Timestamp date2 = new Timestamp(date.getTime() - 10000);
        Event eventDate = new Event(instrument, date2, EventType.UNDEFINED);
        Event eventType = new Event(instrument, date, EventType.CALIBRATION);
        Event eventProblem = new Event(instrument, date, EventType.UNDEFINED, "problem", null, null);
        Event eventSolution = new Event(instrument, date, EventType.UNDEFINED, null, "solution", null);
        Event eventExtra = new Event(instrument, date, EventType.UNDEFINED, null, null, "extra");
        Event eventAttachment = new Event(instrument, date, EventType.UNDEFINED);
        eventAttachment.setAttachment(loadResource("/attachment.jpg"));
        Event eventIdentical = new Event(instrument, date, EventType.UNDEFINED);

        assertEquals(event, event);
        assertNotEquals(event, null);
        assertNotEquals(event, new Object());
        assertNotEquals(event, eventInstrument);
        assertNotEquals(event, eventDate);
        assertNotEquals(event, eventType);
        assertEquals(event, eventProblem);
        assertEquals(event, eventSolution);
        assertEquals(event, eventExtra);
        assertEquals(event, eventAttachment);
        assertEquals(event, eventIdentical);
    }

    private File loadResource(String fileName) {
        try {
            return new File(getClass().getResource(fileName).toURI());
        } catch(URISyntaxException e) {
            fail(e.getMessage());
        }
        return null;
    }
}
