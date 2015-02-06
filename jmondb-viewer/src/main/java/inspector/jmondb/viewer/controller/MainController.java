package inspector.jmondb.viewer.controller;

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

import com.jtattoo.plaf.fast.FastLookAndFeel;
import inspector.jmondb.viewer.controller.listeners.*;
import inspector.jmondb.viewer.model.VisualizationConfiguration;
import inspector.jmondb.viewer.view.gui.ViewerFrame;
import inspector.jmondb.viewer.viewmodel.EventsViewModel;
import inspector.jmondb.viewer.viewmodel.InstrumentsViewModel;
import inspector.jmondb.viewer.viewmodel.MetadataViewModel;
import inspector.jmondb.viewer.viewmodel.PropertiesViewModel;

import javax.swing.*;
import java.util.Properties;

public class MainController {

    private ViewerFrame viewer;

    public MainController() {
        // model
        VisualizationConfiguration visualizationConfiguration = new VisualizationConfiguration();

        // view
        viewer = new ViewerFrame(visualizationConfiguration);

        // view model
        InstrumentsViewModel instrumentsViewModel = new InstrumentsViewModel(viewer.getPropertySelectionPanel());
        PropertiesViewModel propertiesViewModel = new PropertiesViewModel(viewer.getPropertySelectionPanel());
        MetadataViewModel metadataViewModel = new MetadataViewModel();
        EventsViewModel eventsViewModel = new EventsViewModel(viewer.getEventPanel().getEventTree(),
                viewer.getGraphPanel().getEventMarkers(), viewer.getEventPanel().getEventConfigurationPanel());

        // controller
        SearchSettingsController searchSettingsController = new SearchSettingsController(
                instrumentsViewModel, propertiesViewModel, metadataViewModel);
        GraphController graphController = new GraphController(viewer,
                instrumentsViewModel, propertiesViewModel, eventsViewModel, metadataViewModel);
        EventController eventController = new EventController(instrumentsViewModel, eventsViewModel);
        DatabaseController databaseController = new DatabaseController(
                searchSettingsController, graphController, eventController);

        // listeners
        viewer.addExitAction(new ExitAction(databaseController));
        viewer.addAboutDisplayer(new AboutListener(viewer));

        viewer.addDatabaseConnector(new DatabaseConnectListener(viewer, databaseController, searchSettingsController));
        viewer.addDatabaseDisconnector(new DatabaseDisconnectListener(viewer, databaseController));

        GraphShowListener graphShowListener = new GraphShowListener(viewer, searchSettingsController, graphController);
        viewer.addGraphDisplayer(graphShowListener);
        viewer.addGraphSaver(new GraphSaveListener(viewer));

        viewer.addInstrumentChangeListener(new InstrumentChangeListener(
                searchSettingsController, eventController, graphController));
        viewer.addPropertyChangeListener(new PropertyChangeListener(viewer, searchSettingsController));
        viewer.addEventCheckBoxListener(new EventCategoryDisplayer(viewer, graphController));

        viewer.addEventCreator(new EventCreateListener(viewer, instrumentsViewModel, eventController));
        viewer.addEventRemover(new EventRemoveListener(viewer, eventController));
        viewer.addEventEditor(new EventEditListener(viewer, instrumentsViewModel, eventController));
        viewer.addEventClearer(new EventClearListener(viewer, eventController));
        viewer.addEventExporter(new EventExportListener(viewer, eventController));

        viewer.addAdvancedSearchDisplayer(new AdvancedSearchListener(viewer,
                propertiesViewModel, metadataViewModel, searchSettingsController, graphShowListener));

        viewer.addUpdateChecker(new UpdateListener(viewer));

        viewer.addPreferencesDisplayer(new PreferencesListener(viewer, visualizationConfiguration));
    }

    public static void main(String[] args) {
        setLookAndFeel();

        // start viewer
        SwingUtilities.invokeLater(() -> {
            MainController controller = new MainController();
            controller.viewer.initialize();
            controller.viewer.display();
        });
    }

    private static void setLookAndFeel() {
        try {
            // JTattoo fast look and feel
            Properties properties = new Properties();
            properties.put("logoString", "");
            FastLookAndFeel.setCurrentTheme(properties);
            UIManager.setLookAndFeel("com.jtattoo.plaf.fast.FastLookAndFeel");
        } catch (Exception e) {
            // fall back to cross-platform
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch(Exception ignored) {
            }
        }
    }
}
