package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.DatabaseController;
import inspector.jmondb.viewer.view.gui.DatabaseConnectionPanel;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DatabaseConnectListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private DatabaseController databaseController;

    public DatabaseConnectListener(ViewerFrame viewerFrame, DatabaseController databaseController) {
        this.viewerFrame = viewerFrame;
        this.databaseController = databaseController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DatabaseConnectionPanel connectionPanel = new DatabaseConnectionPanel();
        int option = JOptionPane.showConfirmDialog(viewerFrame.getFrame(), connectionPanel.getPanel(),
                "Connect to the database", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if(option == JOptionPane.OK_OPTION) {
            try {
                databaseController.connect(connectionPanel.getHost(), connectionPanel.getPort(),
                        connectionPanel.getDatabase(), connectionPanel.getUserName(), connectionPanel.getPassword());

                // show the connection information
                viewerFrame.getDatabasePanel().setConnected(connectionPanel.getHost(),
                        connectionPanel.getDatabase(), connectionPanel.getUserName());

            } catch(Exception ex) {
                // something went wrong, disconnect to free up the resources
                databaseController.disconnect();

                JOptionPane.showMessageDialog(viewerFrame.getFrame(),
                        "<html><b>Could not connect to the database</b></html>\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
