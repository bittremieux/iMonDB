package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.EventController;
import inspector.jmondb.viewer.model.DatabaseConnection;
import inspector.jmondb.viewer.view.gui.EventDialog;
import inspector.jmondb.viewer.view.gui.ViewerFrame;
import inspector.jmondb.viewer.viewmodel.InstrumentsViewModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EventCreateListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private InstrumentsViewModel instrumentsViewModel;

    private EventController eventController;

    public EventCreateListener(ViewerFrame viewerFrame, InstrumentsViewModel instrumentsViewModel, EventController eventController) {
        this.viewerFrame = viewerFrame;
        this.instrumentsViewModel = instrumentsViewModel;
        this.eventController = eventController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!DatabaseConnection.getConnection().isActive()) {
            JOptionPane.showMessageDialog(viewerFrame.getFrame(),
                    "Please connect to a database before creating a new event.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            EventDialog dialog = new EventDialog(instrumentsViewModel.getActiveInstrument());

            int option = JOptionPane.showConfirmDialog(viewerFrame.getFrame(), dialog.getPanel(),
                    "Create event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if(option == JOptionPane.OK_OPTION) {
                try {
                    eventController.createEvent(dialog.getInstrumentName(), dialog.getDate(), dialog.getType(),
                            dialog.getProblem(), dialog.getSolution(), dialog.getExtra(),
                            dialog.getAttachmentName(), dialog.getAttachmentContent());
                } catch(NullPointerException ex) {
                    JOptionPane.showMessageDialog(viewerFrame.getFrame(), ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
