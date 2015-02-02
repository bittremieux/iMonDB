package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.view.gui.EventDialog;
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
