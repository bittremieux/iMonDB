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
