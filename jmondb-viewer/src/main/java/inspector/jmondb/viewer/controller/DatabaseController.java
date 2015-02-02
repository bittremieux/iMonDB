package inspector.jmondb.viewer.controller;

import inspector.jmondb.viewer.model.DatabaseConnection;

public class DatabaseController {

    private SearchSettingsController searchSettingsController;
    private GraphController graphController;
    private EventController eventController;

    public DatabaseController(SearchSettingsController searchSettingsController,
                              GraphController graphController, EventController eventController) {

        this.searchSettingsController = searchSettingsController;
        this.graphController = graphController;
        this.eventController = eventController;
    }

    public void disconnect() {
        // disconnect from the database
        DatabaseConnection.getConnection().disconnect();

        // remove instruments and properties
        searchSettingsController.clearInstruments();
        searchSettingsController.clearProperties();
        searchSettingsController.clearMetadata();

        // remove the active graph and events
        graphController.clear();
        eventController.clearEvents();
    }

    public void connect(String host, String port, String database, String userName, String password) {
        // first close an existing connection
        disconnect();

        // establish the new connection
        DatabaseConnection.getConnection().connectTo(host, port, database, userName, password);

        // set instruments
        searchSettingsController.setAllInstruments();
        // properties and metadata are set in the InstrumentChangeListener
    }
}
