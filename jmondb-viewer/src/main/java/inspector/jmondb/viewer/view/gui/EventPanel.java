package inspector.jmondb.viewer.view.gui;

import javax.swing.*;
import java.awt.*;

public class EventPanel {

    private JPanel panel;

    private EventConfigurationPanel configurationPanel;
    private EventTree eventTree;

    private JButton buttonAdd;
    private JButton buttonExport;
    private JButton buttonRemove;
    private JButton buttonClear;

    public EventPanel() {
        panel = new JPanel();
        BorderLayout eventsLayout = new BorderLayout();
        eventsLayout.setVgap(25);
        panel.setLayout(eventsLayout);

        // create configuration checkboxes
        configurationPanel = new EventConfigurationPanel();
        panel.add(configurationPanel.getPanel(), BorderLayout.PAGE_START);

        // create event tree
        eventTree = new EventTree();
        panel.add(eventTree.getPanel(), BorderLayout.CENTER);

        // create buttons to manipulate events
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2));
        buttonsPanel.setPreferredSize(new Dimension(250, 50));
        buttonAdd = new JButton("Add");
        buttonsPanel.add(buttonAdd);
        buttonExport = new JButton("Export");
        buttonsPanel.add(buttonExport);
        buttonRemove = new JButton("Remove");
        buttonsPanel.add(buttonRemove);
        buttonClear = new JButton("Clear");
        buttonsPanel.add(buttonClear);

        panel.add(buttonsPanel, BorderLayout.PAGE_END);
    }

    public JPanel getPanel() {
        return panel;
    }

    public EventConfigurationPanel getEventConfigurationPanel() {
        return configurationPanel;
    }

    public EventTree getEventTree() {
        return eventTree;
    }

    public JButton getButtonAdd() {
        return buttonAdd;
    }

    public JButton getButtonRemove() {
        return buttonRemove;
    }

    public JButton getButtonClear() {
        return buttonClear;
    }

    public JButton getButtonExport() {
        return buttonExport;
    }
}
