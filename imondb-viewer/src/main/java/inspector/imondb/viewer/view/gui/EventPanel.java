package inspector.imondb.viewer.view.gui;

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

import inspector.imondb.viewer.model.VisualizationConfiguration;

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

    public EventPanel(VisualizationConfiguration configuration) {
        panel = new JPanel();
        BorderLayout eventsLayout = new BorderLayout();
        eventsLayout.setVgap(25);
        panel.setLayout(eventsLayout);

        // create configuration checkboxes
        configurationPanel = new EventConfigurationPanel();
        panel.add(configurationPanel.getPanel(), BorderLayout.PAGE_START);

        // create event tree
        eventTree = new EventTree(configuration);
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
