package inspector.jmondb.viewer.controller;

import inspector.jmondb.model.EventType;
import inspector.jmondb.viewer.model.DatabaseConnection;
import inspector.jmondb.viewer.view.gui.GraphPanel;
import inspector.jmondb.viewer.view.gui.ViewerFrame;
import inspector.jmondb.viewer.viewmodel.EventsViewModel;
import inspector.jmondb.viewer.viewmodel.InstrumentsViewModel;
import inspector.jmondb.viewer.viewmodel.MetadataViewModel;
import inspector.jmondb.viewer.viewmodel.PropertiesViewModel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.Layer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            for(ValueMarker marker : eventsViewModel.getMarkers().getMarkers(type)) {
                if(display && (activeMarkers == null || !activeMarkers.contains(marker))) {
                    plot.addDomainMarker(marker);
                } else if(!display) {
                    plot.removeDomainMarker(marker);
                }
            }
        }
    }
}
