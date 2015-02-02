package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.view.gui.EventDialog;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class AttachmentAddListener implements ActionListener {

    private EventDialog eventDialog;

    public AttachmentAddListener(EventDialog eventDialog) {
        this.eventDialog = eventDialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();

        int returnVal = fileChooser.showOpenDialog(eventDialog.getPanel());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File attachment = fileChooser.getSelectedFile();

            Thread attachmentReader = new Thread() {
                public void run() {
                    try {
                        // file name
                        eventDialog.setAttachmentName(attachment.getName());
                        // file content
                        eventDialog.setAttachmentContent(FileUtils.readFileToByteArray(attachment));
                        // set the icon according to the file type
                        eventDialog.setAttachmentIconFileType();
                    } catch(IOException ex) {
                        JOptionPane.showMessageDialog(eventDialog.getPanel(), ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            attachmentReader.start();
        }
    }
}
