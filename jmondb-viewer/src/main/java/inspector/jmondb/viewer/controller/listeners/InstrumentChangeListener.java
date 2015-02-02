package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.EventController;
import inspector.jmondb.viewer.controller.GraphController;
import inspector.jmondb.viewer.controller.SearchSettingsController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InstrumentChangeListener implements ActionListener {

    private SearchSettingsController searchSettingsController;
    private GraphController graphController;
    private EventController eventController;

    public InstrumentChangeListener(SearchSettingsController searchSettingsController, EventController eventController,
                                    GraphController graphController) {
        this.searchSettingsController = searchSettingsController;
        this.eventController = eventController;
        this.graphController = graphController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        searchSettingsController.clearProperties();
        searchSettingsController.setPropertiesForActiveInstrument();
        searchSettingsController.clearMetadata();
        searchSettingsController.setMetadataOptionsForActiveInstrument();
        eventController.clearEvents();
        eventController.loadEventsForActiveInstrument();
        graphController.clear();
    }
}
