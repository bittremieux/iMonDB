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
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class AttachmentOpenListener extends MouseAdapter {

    private EventDialog eventDialog;

    public AttachmentOpenListener(EventDialog eventDialog) {
        this.eventDialog = eventDialog;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2 &&
                eventDialog.getAttachmentName() != null && eventDialog.getAttachmentContent() != null) {
            Thread opener = new Thread() {
                public void run() {
                    try {
                        // store the attachment in a temporary file
                        String prefix = FilenameUtils.getBaseName(eventDialog.getAttachmentName()) + "_";
                        String suffix = "." + FilenameUtils.getExtension(eventDialog.getAttachmentName());
                        File temp = File.createTempFile(prefix, suffix);
                        temp.deleteOnExit();
                        FileUtils.writeByteArrayToFile(temp, eventDialog.getAttachmentContent());

                        // open the attachment in the default application
                        Desktop.getDesktop().open(temp);
                    } catch(IOException ex) {
                        JOptionPane.showMessageDialog(eventDialog.getPanel(), ex.getMessage(),
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }
            };
            opener.start();
        }
    }
}
