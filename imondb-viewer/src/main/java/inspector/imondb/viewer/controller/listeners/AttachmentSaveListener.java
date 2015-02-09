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

import inspector.imondb.viewer.view.gui.EventDialog;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class AttachmentSaveListener implements ActionListener {

    private EventDialog eventDialog;

    public AttachmentSaveListener(EventDialog eventDialog) {
        this.eventDialog = eventDialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(eventDialog.getAttachmentName() != null && eventDialog.getAttachmentContent() != null) {
            // show save dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(eventDialog.getAttachmentName()));

            int returnVal = fileChooser.showSaveDialog(eventDialog.getPanel());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                // save the attachment to the selected file
                Thread attachmentSaver = new Thread() {
                    public void run() {
                        try {
                            FileUtils.writeByteArrayToFile(fileChooser.getSelectedFile(), eventDialog.getAttachmentContent());
                        } catch(IOException ex) {
                            JOptionPane.showMessageDialog(eventDialog.getPanel(), "Could not save the attachment",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                attachmentSaver.start();
            }
        }
    }
}
