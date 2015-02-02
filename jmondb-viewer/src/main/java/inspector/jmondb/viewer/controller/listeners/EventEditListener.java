package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.model.Event;
import inspector.jmondb.viewer.controller.EventController;
import inspector.jmondb.viewer.view.gui.EventDialog;
import inspector.jmondb.viewer.view.gui.EventNode;
import inspector.jmondb.viewer.view.gui.ViewerFrame;
import inspector.jmondb.viewer.viewmodel.InstrumentsViewModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EventEditListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private InstrumentsViewModel instrumentsViewModel;

    private EventController eventController;

    public EventEditListener(ViewerFrame frame, InstrumentsViewModel instrumentsViewModel, EventController eventController) {
        this.viewerFrame = frame;
        this.instrumentsViewModel = instrumentsViewModel;
        this.eventController = eventController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EventNode node = viewerFrame.getEventPanel().getEventTree().getSelectedEventNode();
        if(node != null) {
            Event event = node.getEvent();
            EventDialog dialog = new EventDialog(instrumentsViewModel.getActiveInstrument(), event);

            int option = JOptionPane.showConfirmDialog(viewerFrame.getFrame(), dialog.getPanel(),
                    "Edit event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if(option == JOptionPane.OK_OPTION) {
                try {
                    eventController.editEvent(event, dialog.getProblem(), dialog.getSolution(), dialog.getExtra(),
                            dialog.getAttachmentName(), dialog.getAttachmentContent());
                } catch(IllegalArgumentException | NullPointerException ex) {
                    JOptionPane.showMessageDialog(viewerFrame.getFrame(), ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
