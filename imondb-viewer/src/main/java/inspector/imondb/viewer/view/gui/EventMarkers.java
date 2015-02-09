package inspector.imondb.viewer.view.gui;

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
import inspector.imondb.viewer.model.VisualizationConfiguration;
import org.jfree.chart.plot.ValueMarker;

import java.awt.*;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventMarkers {

    private Map<EventType, Map<Date, ValueMarker>> markers;

    private GraphPanel graphPanel;

    private VisualizationConfiguration configuration;

    public EventMarkers(GraphPanel graphPanel, VisualizationConfiguration configuration) {
        this.graphPanel = graphPanel;

        this.configuration = configuration;

        markers = new HashMap<>();
        for(EventType type : EventType.values()) {
            markers.put(type, new HashMap<>());
        }
    }

    public void addMarker(Event event) {
        // create a new marker
        ValueMarker marker = new ValueMarker(event.getDate().getTime(), configuration.getColor(event.getType()),
                new BasicStroke(1));
        markers.get(event.getType()).put(event.getDate(), marker);

        // add the marker to the graph
        graphPanel.addEventMarker(marker);
    }

    public void removeMarker(Event event) {
        // remove the marker from the graph
        graphPanel.removeEventMarker(markers.get(event.getType()).get(event.getDate()));

        // remove the marker
        markers.get(event.getType()).remove(event.getDate());
    }

    public Collection<ValueMarker> getMarkers(EventType type) {
        return markers.get(type).values();
    }

    public void clear() {
        for(EventType type : EventType.values()) {
            markers.get(type).values().forEach(graphPanel::removeEventMarker);
            markers.get(type).clear();
        }
    }

    public void refreshColor() {
        for(EventType type : EventType.values()) {
            for(ValueMarker marker : markers.get(type).values()) {
                marker.setPaint(configuration.getColor(type));
            }
        }
    }
}
