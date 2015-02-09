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

import inspector.imondb.viewer.model.DatabaseConnection;

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
