package inspector.jmondb.viewer.controller.listeners;

/*
 * #%L
 * jMonDB Viewer
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
