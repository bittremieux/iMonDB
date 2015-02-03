package inspector.jmondb.viewer.controller;

import inspector.jmondb.viewer.controller.listeners.*;
import inspector.jmondb.viewer.view.gui.EventMarkers;
import inspector.jmondb.viewer.view.gui.ViewerFrame;
import inspector.jmondb.viewer.viewmodel.EventsViewModel;
import inspector.jmondb.viewer.viewmodel.InstrumentsViewModel;
import inspector.jmondb.viewer.viewmodel.MetadataViewModel;
import inspector.jmondb.viewer.viewmodel.PropertiesViewModel;

import javax.swing.*;

public class MainController {

    private ViewerFrame viewer;

    public MainController() {
        // view
        viewer = new ViewerFrame();

        // view model
        InstrumentsViewModel instrumentsViewModel = new InstrumentsViewModel(viewer.getPropertySelectionPanel());
        PropertiesViewModel propertiesViewModel = new PropertiesViewModel(viewer.getPropertySelectionPanel());
        MetadataViewModel metadataViewModel = new MetadataViewModel();
        EventsViewModel eventsViewModel = new EventsViewModel(viewer.getEventPanel().getEventTree(),
                new EventMarkers(viewer.getGraphPanel()), viewer.getEventPanel().getEventConfigurationPanel());

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

        viewer.addGraphDisplayer(new GraphShowListener(viewer, searchSettingsController, graphController));
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
                propertiesViewModel, metadataViewModel, searchSettingsController));
    }

    public static void main(String[] args) {
        setLookAndFeel();

        // start viewer
        SwingUtilities.invokeLater(() -> {
            MainController controller = new MainController();
            controller.viewer.display();
        });
    }

    private static void setLookAndFeel() {
        try {
            // Nimbus look and feel
            for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // if Nimbus is not available, fall back to cross-platform
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }
    }
}
