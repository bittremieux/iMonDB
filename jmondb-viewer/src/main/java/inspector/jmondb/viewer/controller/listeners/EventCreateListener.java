package inspector.jmondb.viewer.controller.listeners;

/*
 * #%L
 * jMonDB Viewer
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
