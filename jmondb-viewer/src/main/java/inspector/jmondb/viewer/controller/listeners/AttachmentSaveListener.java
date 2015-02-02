package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.view.gui.EventDialog;
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
