package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.SearchSettingsController;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PropertyChangeListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private SearchSettingsController searchSettingsController;

    public PropertyChangeListener(ViewerFrame viewerFrame, SearchSettingsController searchSettingsController) {
        this.viewerFrame = viewerFrame;
        this.searchSettingsController = searchSettingsController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // edit button state
        viewerFrame.getGraphPanel().setNextButtonEnabled(searchSettingsController.hasNextProperty());
        viewerFrame.getGraphPanel().setPreviousButtonEnabled(searchSettingsController.hasPreviousProperty());
    }
}
