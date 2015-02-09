package inspector.imondb.viewer.controller;

/*
 * #%L
 * iMonDB Viewer
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

import inspector.imondb.model.Event;
import inspector.imondb.model.EventType;
import inspector.imondb.model.Instrument;
import inspector.imondb.viewer.model.DatabaseConnection;
import inspector.imondb.viewer.view.EventsReportWriter;
import inspector.imondb.viewer.viewmodel.EventsViewModel;
import inspector.imondb.viewer.viewmodel.InstrumentsViewModel;
import net.sf.dynamicreports.report.exception.DRException;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

public class EventController {

    private InstrumentsViewModel instrumentsViewModel;
    private EventsViewModel eventsViewModel;

    public EventController(InstrumentsViewModel instrumentsViewModel, EventsViewModel view) {
        this.instrumentsViewModel = instrumentsViewModel;
        this.eventsViewModel = view;
    }

    public void loadEventsForActiveInstrument() {
        if(DatabaseConnection.getConnection().isActive()) {
            // add new events to the application
            Instrument instrument = DatabaseConnection.getConnection().getReader().getInstrument(
                    instrumentsViewModel.getActiveInstrument(), true, false);
            for(Iterator<Event> it = instrument.getEventIterator(); it.hasNext(); ) {
                addEvent(it.next());
            }
        }
    }

    public void createEvent(String instrumentName, Timestamp date, EventType type, String problem, String solution,
                            String extra, String attachmentName, byte[] attachmentContent) {
        if(DatabaseConnection.getConnection().isActive()) {
            // create the new event
            Instrument instrument = DatabaseConnection.getConnection().getReader().getInstrument(
                    instrumentName, true, false);
            Event event = new Event(instrument, date, type, problem, solution, extra);
            if(attachmentName != null && attachmentContent != null) {
                event.setAttachmentName(attachmentName);
                event.setAttachmentContent(attachmentContent);
            }

            // write the event to the database
            DatabaseConnection.getConnection().getWriter().writeOrUpdateEvent(event);

            // add the event to the application
            addEvent(event);
        }
    }

    public void addEvent(Event event) {
        eventsViewModel.add(event);
    }

    public void editEvent(Event event, String problem, String solution, String extra,
                          String attachmentName, byte[] attachmentContent) {
        if(DatabaseConnection.getConnection().isActive()) {
            boolean isChanged = false;

            if(event.getProblem() != null ? !event.getProblem().equals(problem) : problem != null) {
                event.setProblem(problem);
                isChanged = true;
            }
            if(event.getSolution() != null ? !event.getSolution().equals(solution) : solution != null) {
                event.setSolution(solution);
                isChanged = true;
            }
            if(event.getExtra() != null ? !event.getExtra().equals(extra) : extra != null) {
                event.setExtra(extra);
                isChanged = true;
            }
            if(event.getAttachmentName() != null && event.getAttachmentContent() != null ?
                    !event.getAttachmentName().equals(attachmentName) ||
                            !Arrays.equals(event.getAttachmentContent(), attachmentContent) :
                    attachmentName != null || attachmentContent != null) {
                event.setAttachmentName(attachmentName);
                event.setAttachmentContent(attachmentContent);
                isChanged = true;
            }

            if(isChanged) {
                DatabaseConnection.getConnection().getWriter().writeOrUpdateEvent(event);
            }
        }
    }

    public void clearEvents() {
        eventsViewModel.clearAll();
    }

    public void removeEvent(Event event) {
        if(DatabaseConnection.getConnection().isActive()) {
            // remove from the database
            DatabaseConnection.getConnection().getWriter().removeEvent(
                    instrumentsViewModel.getActiveInstrument(), event.getDate());

            // remove from the application
            eventsViewModel.remove(event);
        }
    }

    public void deleteEvents() {
        if(DatabaseConnection.getConnection().isActive()) {
            // remove from the database
            List<Event> events = eventsViewModel.getAll();
            for(Event event : events) {
                DatabaseConnection.getConnection().getWriter().removeEvent(
                        instrumentsViewModel.getActiveInstrument(), event.getDate());
            }

            clearEvents();
        }
    }

    public void exportEvents(File file) {
        if(DatabaseConnection.getConnection().isActive()) {
            // get all events in chronological order
            Instrument instrument = DatabaseConnection.getConnection().getReader().getInstrument(
                    instrumentsViewModel.getActiveInstrument(), true, false);
            List<Event> events = new ArrayList<>();
            for(Iterator<Event> it = instrument.getEventIterator(); it.hasNext(); ) {
                events.add(it.next());
            }
            Collections.sort(events, new Comparator<Event>() {
                @Override
                public int compare(Event o1, Event o2) {
                    int compareDate = o1.getDate().compareTo(o2.getDate());
                    return compareDate == 0 ? o1.getType().compareTo(o2.getType()) : compareDate;
                }
            });

            if(events.size() == 0) {
                throw new IllegalArgumentException("No events found for instrument <" +
                        instrumentsViewModel.getActiveInstrument() + ">");
            } else {
                try {
                    EventsReportWriter.writeReport(instrumentsViewModel.getActiveInstrument(), events, file);
                } catch(DRException | IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
    }
}
