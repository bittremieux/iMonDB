package inspector.jmondb.viewer.view.gui;

import inspector.jmondb.model.Event;
import inspector.jmondb.viewer.controller.listeners.EventTreeMouseListener;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.awt.event.ActionListener;

public class EventTree {

    private JPanel panel;

    private DefaultMutableTreeNode nodeUndefined;
    private DefaultMutableTreeNode nodeCalibration;
    private DefaultMutableTreeNode nodeMaintenance;
    private DefaultMutableTreeNode nodeIncident;

    private JTree treeEvents;

    private EventTreeMouseListener mouseListener;

    public EventTree() {
        panel = new JPanel(new BorderLayout());

        // create all tree nodes
        DefaultMutableTreeNode nodeEvents = new DefaultMutableTreeNode("Events");
        nodeUndefined = new DefaultMutableTreeNode("Undefined");
        nodeEvents.add(nodeUndefined);
        nodeCalibration = new DefaultMutableTreeNode("Calibration");
        nodeEvents.add(nodeCalibration);
        nodeMaintenance = new DefaultMutableTreeNode("Maintenance");
        nodeEvents.add(nodeMaintenance);
        nodeIncident = new DefaultMutableTreeNode("Incident");
        nodeEvents.add(nodeIncident);

        // create the event tree
        treeEvents = new JTree(nodeEvents);
        treeEvents.setCellRenderer(new EventTreeCellRenderer());

        // scroll pane for long event lists
        JScrollPane scrollPaneEvents = new JScrollPane(treeEvents);
        panel.add(scrollPaneEvents, BorderLayout.CENTER);

        // mouse listener to create context menus on right click
        mouseListener = new EventTreeMouseListener(this);
        treeEvents.addMouseListener(mouseListener);
    }

    public void setEventRemoveListener(ActionListener listener) {
        mouseListener.setEventRemoveListener(listener);
    }

    public void setEventEditListener(ActionListener listener) {
        mouseListener.setEventEditListener(listener);
    }

    public JPanel getPanel() {
        return panel;
    }

    public JTree getTree() {
        return treeEvents;
    }

    public EventNode getSelectedEventNode() {
        return (treeEvents.getSelectionPath() != null && treeEvents.getSelectionPath().getLastPathComponent() instanceof EventNode) ?
                (EventNode) treeEvents.getSelectionPath().getLastPathComponent() :
                null;
    }

    public void addNode(Event event) {
        // find the parent node corresponding to the event type
        MutableTreeNode parent = getEventParentNode(event);

        // add the new event node
        EventNode eventNode = new EventNode(event);
        ((DefaultTreeModel) treeEvents.getModel()).insertNodeInto(eventNode, parent, getNodeInsertIndex(parent, eventNode));
    }

    private MutableTreeNode getEventParentNode(Event event) {
        MutableTreeNode parent;
        switch(event.getType()) {
            case UNDEFINED:
                parent = nodeUndefined;
                break;
            case CALIBRATION:
                parent = nodeCalibration;
                break;
            case MAINTENANCE:
                parent = nodeMaintenance;
                break;
            case INCIDENT:
                parent = nodeIncident;
                break;
            default:
                parent = null;
                break;
        }
        return parent;
    }

    private int getNodeInsertIndex(MutableTreeNode parent, EventNode newChild) {
        for(int i = 0; i < parent.getChildCount(); i++) {
            if(((EventNode) parent.getChildAt(i)).compareTo(newChild) > 0) {
                return i;
            }
        }
        return parent.getChildCount();
    }

    public void removeNode(Event event) {
        ((DefaultTreeModel) treeEvents.getModel()).removeNodeFromParent(getNodeForEvent(event));
    }

    private EventNode getNodeForEvent(Event event) {
        // find the parent node corresponding to the event type
        MutableTreeNode parent = getEventParentNode(event);

        // find the node corresponding to the event
        for(int i = 0; i < parent.getChildCount(); i++) {
            EventNode node = (EventNode) parent.getChildAt(i);
            if(node.getEvent().equals(event)) {
                return node;
            }
        }
        return null;
    }

    public void expand() {
        for(int i = 0; i < treeEvents.getRowCount(); i++)
            treeEvents.expandRow(i);
    }

    public void clear() {
        treeEvents.clearSelection();
        nodeIncident.removeAllChildren();
        nodeMaintenance.removeAllChildren();
        nodeCalibration.removeAllChildren();
        nodeUndefined.removeAllChildren();

        ((DefaultTreeModel) treeEvents.getModel()).reload();
    }
}
