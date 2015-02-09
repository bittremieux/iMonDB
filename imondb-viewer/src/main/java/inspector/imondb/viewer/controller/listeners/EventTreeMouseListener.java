package inspector.imondb.viewer.controller.listeners;

/*
 * #%L
 * iMonDB Viewer
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

import inspector.imondb.viewer.view.gui.EventNode;
import inspector.imondb.viewer.view.gui.EventTree;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EventTreeMouseListener extends MouseAdapter {

    private EventTree eventTree;

    private ActionListener eventEditListener;
    private ActionListener eventRemoveListener;

    public EventTreeMouseListener(EventTree eventTree) {
        this.eventTree = eventTree;
    }

    public void setEventEditListener(ActionListener listener) {
        this.eventEditListener = listener;
    }

    public void setEventRemoveListener(ActionListener listener) {
        this.eventRemoveListener = listener;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)) {
            // highlight relevant node
            int row = eventTree.getTree().getClosestRowForLocation(e.getX(), e.getY());
            eventTree.getTree().setSelectionRow(row);

            EventNode selectedNode = eventTree.getSelectedEventNode();
            if(selectedNode != null) {
                // show pop-up menu
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem itemEdit = new JMenuItem("Edit");
                itemEdit.addActionListener(eventEditListener);
                popupMenu.add(itemEdit);
                JMenuItem itemRemove = new JMenuItem("Remove");
                itemRemove.addActionListener(eventRemoveListener);
                popupMenu.add(itemRemove);

                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        } else if(e.getClickCount() == 2 && eventEditListener != null) {
            eventEditListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }
    }
}
