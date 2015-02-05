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

import inspector.jmondb.viewer.controller.DatabaseController;
import inspector.jmondb.viewer.controller.SearchSettingsController;
import inspector.jmondb.viewer.view.gui.DatabaseConnectionDialog;
import inspector.jmondb.viewer.view.gui.DatabaseConnectionPanel;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class DatabaseConnectListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private DatabaseController databaseController;

    private SearchSettingsController searchSettingsController;

    public DatabaseConnectListener(ViewerFrame viewerFrame, DatabaseController databaseController,
                                   SearchSettingsController searchSettingsController) {
        this.viewerFrame = viewerFrame;
        this.databaseController = databaseController;
        this.searchSettingsController = searchSettingsController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DatabaseConnectionPanel connectionPanel = new DatabaseConnectionPanel();
        int option = JOptionPane.showConfirmDialog(viewerFrame.getFrame(), connectionPanel.getPanel(),
                "Connect to the database", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if(option == JOptionPane.OK_OPTION) {
            DatabaseConnectionDialog dialog = new DatabaseConnectionDialog(viewerFrame.getFrame(),
                    connectionPanel.getHost(), connectionPanel.getDatabase(), connectionPanel.getUserName());

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    databaseController.connect(connectionPanel.getHost(), connectionPanel.getPort(),
                            connectionPanel.getDatabase(), connectionPanel.getUserName(), connectionPanel.getPassword());

                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();

                        dialog.hideDialog(false);
                    } catch(InterruptedException | ExecutionException ex) {
                        dialog.hideDialog(true);

                        JOptionPane.showMessageDialog(viewerFrame.getFrame(),
                                "<html><b>Could not connect to the database</b></html>\n" + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    } catch(CancellationException ignored) {
                    }
                }
            };

            // establish the connection
            worker.execute();

            // show a progress bar dialog
            if(dialog.showDialog()) {
                worker.cancel(true);

                // something went wrong, disconnect to free up the resources
                databaseController.disconnect();

                // indicate not connected status
                viewerFrame.getDatabasePanel().setNotConnected();

                // disable graph advance buttons
                viewerFrame.getGraphPanel().setNextButtonEnabled(false);
                viewerFrame.getGraphPanel().setPreviousButtonEnabled(false);
            } else {
                // show the connection information
                viewerFrame.getDatabasePanel().setConnected(connectionPanel.getHost(),
                        connectionPanel.getDatabase(), connectionPanel.getUserName());

                // enable graph advance buttons
                viewerFrame.getGraphPanel().setNextButtonEnabled(searchSettingsController.hasNextProperty());
                viewerFrame.getGraphPanel().setPreviousButtonEnabled(searchSettingsController.hasPreviousProperty());
            }
        }
    }
}
