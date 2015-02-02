package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.view.gui.EventNode;
import inspector.jmondb.viewer.view.gui.EventTree;

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
