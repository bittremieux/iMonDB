package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.DatabaseController;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DatabaseDisconnectListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private DatabaseController databaseController;

    public DatabaseDisconnectListener(ViewerFrame viewerFrame, DatabaseController databaseController) {
        this.viewerFrame = viewerFrame;
        this.databaseController = databaseController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        databaseController.disconnect();

        // indicate not connected status
        viewerFrame.getDatabasePanel().setNotConnected();
    }
}
