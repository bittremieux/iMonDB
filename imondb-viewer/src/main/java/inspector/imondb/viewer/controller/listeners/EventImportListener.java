package inspector.imondb.viewer.controller.listeners;

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

import inspector.imondb.viewer.controller.EventController;
import inspector.imondb.viewer.model.DatabaseConnection;
import inspector.imondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EventImportListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private EventController eventController;

    public EventImportListener(ViewerFrame frame, EventController eventController) {
        this.viewerFrame = frame;
        this.eventController = eventController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!DatabaseConnection.getConnection().isActive()) {
            JOptionPane.showMessageDialog(viewerFrame.getFrame(),
                    "Please connect to a database to import events.", "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            try {
                JFileChooser fileChooser = new JFileChooser();
                // import only from csv files
                FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV-files", "csv");
                fileChooser.addChoosableFileFilter(csvFilter);
                fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());

                int returnVal = fileChooser.showSaveDialog(viewerFrame.getFrame());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    Thread eventImporter = new Thread() {
                        public void run() {
                            eventController.importEvents(fileChooser.getSelectedFile());
                        }
                    };
                    eventImporter.start();
                }
            } catch(IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(viewerFrame.getFrame(), ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            } catch(IllegalStateException ex) {
                JOptionPane.showMessageDialog(viewerFrame.getFrame(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
