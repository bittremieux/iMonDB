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
import inspector.imondb.viewer.view.gui.GraphPanel;
import inspector.imondb.viewer.view.gui.ViewerFrame;
import inspector.imondb.viewer.viewmodel.EventsViewModel;
import inspector.imondb.viewer.viewmodel.InstrumentsViewModel;
import inspector.imondb.viewer.viewmodel.MetadataViewModel;
import inspector.imondb.viewer.viewmodel.PropertiesViewModel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.Layer;

import java.util.*;

public class GraphController {

    private GraphPanel graphPanel;

    private InstrumentsViewModel instrumentsViewModel;
    private PropertiesViewModel propertiesViewModel;
    private EventsViewModel eventsViewModel;
    private MetadataViewModel metadataViewModel;

    public GraphController(ViewerFrame frame, InstrumentsViewModel instrumentsViewModel, PropertiesViewModel propertiesViewModel,
                           EventsViewModel eventsViewModel, MetadataViewModel metadataViewModel) {
        this.graphPanel = frame.getGraphPanel();

        this.instrumentsViewModel = instrumentsViewModel;
        this.propertiesViewModel = propertiesViewModel;
        this.eventsViewModel = eventsViewModel;
        this.metadataViewModel = metadataViewModel;
    }

    public List<Object[]> queryValues() {
        if(DatabaseConnection.getConnection().isActive() && propertiesViewModel.hasValidProperty()) {
            // load all values for the property and instrument
            StringBuilder querySelectFrom = new StringBuilder(
                    "SELECT val, val.originatingRun.sampleDate FROM Value val");
            StringBuilder queryWhere = new StringBuilder(
                    "WHERE val.originatingRun.instrument.name = :instName AND " +
                            "val.definingProperty.accession = :propAccession");
            Map<String, String> parameters = new HashMap<>();
            parameters.put("instName", instrumentsViewModel.getActiveInstrument());
            parameters.put("propAccession", propertiesViewModel.getActivePropertyAccession());

            // add metadata filters
            if(metadataViewModel.getMetadataFilter() != null) {
                metadataViewModel.getMetadataFilter().toQuery(querySelectFrom, queryWhere, parameters);
            }

            String query = querySelectFrom.toString() + " " + queryWhere.toString() +
                    " ORDER BY val.originatingRun.sampleDate";

            return DatabaseConnection.getConnection().getReader().getFromCustomQuery(query, Object[].class, parameters);
        } else {
            return null;
        }
    }

    public List<Object[]> queryExternalValues() {
        if(DatabaseConnection.getConnection().isActive() &&
                instrumentsViewModel.getActiveExternal() != null && !"(none)".equals(instrumentsViewModel.getActiveExternal())) {
            Instrument external = DatabaseConnection.getConnection().getReader().getInstrument(
                    instrumentsViewModel.getActiveExternal(), true, false);
            List<Event> events = new ArrayList<>();
            for(Iterator<Event> it = external.getEventIterator(); it.hasNext(); ) {
                Event event = it.next();
                if(event.getType() == EventType.TEMPERATURE) {
                    events.add(event);
                }
            }
            Collections.sort(events, (o1, o2) -> {
                int compareDate = o1.getDate().compareTo(o2.getDate());
                return compareDate == 0 ? o1.getType().compareTo(o2.getType()) : compareDate;
            });
            List<Object[]> result = new ArrayList<>(events.size());
            for(Event event : events) {
                result.add(new Object[] { Double.parseDouble(event.getExtra()), event.getDate() });
            }

            return result;
        } else {
            return null;
        }
    }

    public void clear() {
        graphPanel.clearChart();
        graphPanel.setTitle("");

        // disable graph advance buttons
        graphPanel.setNextButtonEnabled(false);
        graphPanel.setPreviousButtonEnabled(false);
    }

    public void display(JFreeChart chart) {
        chart.setTitle(propertiesViewModel.getActivePropertyName());
        graphPanel.displayChart(chart);
        graphPanel.setTitle(propertiesViewModel.getActivePropertyName());
    }

    public void displayEvents() {
        if(graphPanel.getChartPanel().getChart() != null) {
            ((XYPlot) graphPanel.getChartPanel().getChart().getPlot()).clearDomainMarkers();

            for(EventType type : eventsViewModel.getDisplayedEventTypes()) {
                displayEventCategory(type, true);
            }
        }
    }

    public void displayEventCategory(EventType type, boolean display) {
        if(graphPanel.getChartPanel().getChart() != null) {
            XYPlot plot = (XYPlot) graphPanel.getChartPanel().getChart().getPlot();
            Collection activeMarkers = plot.getDomainMarkers(Layer.FOREGROUND);

            for(ValueMarker marker : graphPanel.getEventMarkers().getMarkers(type)) {
                if(display && (activeMarkers == null || !activeMarkers.contains(marker))) {
                    plot.addDomainMarker(marker);
                } else if(!display) {
                    plot.removeDomainMarker(marker);
                }
            }
        }
    }
}
