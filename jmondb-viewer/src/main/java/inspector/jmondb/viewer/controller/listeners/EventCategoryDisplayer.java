package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.model.EventType;
import inspector.jmondb.viewer.controller.GraphController;
import inspector.jmondb.viewer.view.gui.EventConfigurationPanel;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class EventCategoryDisplayer implements ItemListener {

    private EventConfigurationPanel eventConfigurationPanel;

    private GraphController graphController;

    public EventCategoryDisplayer(ViewerFrame viewer, GraphController graphController) {
        this.eventConfigurationPanel = viewer.getEventPanel().getEventConfigurationPanel();

        this.graphController = graphController;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        ItemSelectable source = e.getItemSelectable();

        EventType category = eventConfigurationPanel.getCheckBoxEventType((JCheckBox) source);
        boolean display = e.getStateChange() == ItemEvent.SELECTED;

        graphController.displayEventCategory(category, display);
    }
}
