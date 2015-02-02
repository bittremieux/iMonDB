package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.view.gui.EventDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AttachmentRemoveListener implements ActionListener {

    private EventDialog eventDialog;

    public AttachmentRemoveListener(EventDialog eventDialog) {
        this.eventDialog = eventDialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(eventDialog.getAttachmentName() != null || eventDialog.getAttachmentContent() != null) {
            eventDialog.setAttachmentName("No attachment added");
            eventDialog.setAttachmentContent(null);
            eventDialog.setAttachmentIconFileType();    // no-file icon
        }
    }
}
