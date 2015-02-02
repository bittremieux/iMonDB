package inspector.jmondb.viewer.view.gui;

import inspector.jmondb.model.Event;
import inspector.jmondb.model.EventType;
import org.jfree.chart.plot.ValueMarker;

import java.awt.*;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventMarkers {

    //TODO: move the colors to a settings class
    private static final Color COLOR_UNDEFINED = Color.ORANGE;
    private static final Color COLOR_CALIBRATION = Color.GREEN;
    private static final Color COLOR_MAINTENANCE = Color.BLUE;
    private static final Color COLOR_INCIDENT = Color.RED;

    private Map<EventType, Map<Date, ValueMarker>> markers;

    private GraphPanel graphPanel;

    public EventMarkers(GraphPanel graphPanel) {
        this.graphPanel = graphPanel;

        markers = new HashMap<>();
        for(EventType type : EventType.values()) {
            markers.put(type, new HashMap<>());
        }
    }

    public void addMarker(Event event) {
        Color color;
        switch(event.getType()) {
            case UNDEFINED:
                color = COLOR_UNDEFINED;
                break;
            case CALIBRATION:
                color = COLOR_CALIBRATION;
                break;
            case MAINTENANCE:
                color = COLOR_MAINTENANCE;
                break;
            case INCIDENT:
                color = COLOR_INCIDENT;
                break;
            default:
                color = Color.BLACK;
                break;
        }

        // create a new marker
        ValueMarker marker = new ValueMarker(event.getDate().getTime(), color, new BasicStroke(1));
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
}
