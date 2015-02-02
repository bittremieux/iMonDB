package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.EventController;
import inspector.jmondb.viewer.model.DatabaseConnection;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EventClearListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private EventController eventController;

    public EventClearListener(ViewerFrame viewerFrame, EventController eventController) {
        this.viewerFrame = viewerFrame;
        this.eventController = eventController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(DatabaseConnection.getConnection().isActive()) {
            int option = JOptionPane.showConfirmDialog(viewerFrame.getFrame(),
                    "Attention: This will permanently remove all events from the database!",
                    "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

            if(option == JOptionPane.OK_OPTION) {
                eventController.deleteEvents();
            }
        }
    }
}
