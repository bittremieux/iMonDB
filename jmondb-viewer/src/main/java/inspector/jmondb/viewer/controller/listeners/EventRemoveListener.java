package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.EventController;
import inspector.jmondb.viewer.view.gui.EventNode;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EventRemoveListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private EventController eventController;

    public EventRemoveListener(ViewerFrame frame, EventController eventController) {
        this.viewerFrame = frame;
        this.eventController = eventController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EventNode selectedNode = viewerFrame.getEventPanel().getEventTree().getSelectedEventNode();
        if(selectedNode != null) {
            int option = JOptionPane.showConfirmDialog(viewerFrame.getFrame(),
                    "Attention: The event will be removed from the database as well.",
                    "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

            if(option == JOptionPane.OK_OPTION) {
                try {
                    eventController.removeEvent(selectedNode.getEvent());
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(viewerFrame.getFrame(), ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
