package inspector.imondb.collector.view.metadata;

/*
 * #%L
 * iMonDB Collector
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

import inspector.imondb.collector.controller.listeners.ConfigurationChangeListener;
import inspector.imondb.collector.model.MetadataMap;
import inspector.imondb.collector.view.instrument.RegexMapTestPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class MetadataPanel extends Observable {

    private JPanel panel;

    private JButton buttonAdd;
    private JButton buttonTest;

    private JScrollPane scrollPaneMetadata;
    private JPanel panelMetadata;
    private Map<MetadataMap, MetadataOverviewPanel> metadataMaps;

    public MetadataPanel() {
        metadataMaps = new HashMap<>();

        buttonAdd.addActionListener(e -> {
            MetadataCreatePanel metadataCreatePanel = new MetadataCreatePanel();
            int result = JOptionPane.showConfirmDialog(Frame.getFrames()[0], metadataCreatePanel.getPanel(),
                    "Add metadata", JOptionPane.OK_CANCEL_OPTION);

            if(result == JOptionPane.OK_OPTION) {
                MetadataMap metadataMap = metadataCreatePanel.getMetadataMap();

                if(metadataMap.isValid() && !metadataMaps.containsKey(metadataMap)) {
                    addMetadata(metadataMap);

                    panelMetadata.revalidate();
                    panelMetadata.repaint();
                    scrollPaneMetadata.revalidate();
                } else {
                    JOptionPane.showMessageDialog(Frame.getFrames()[0],
                            "<html>Invalid metadata configuration.<br><br>Please try to add the metadata again.<br>" +
                                    "Make sure that all fields are correctly set,<br>" +
                                    "and that no other metadata with the<br>same key-value combination already exists.</html>",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonTest.addActionListener(e -> {
            RegexMapTestPanel regexMapTestPanel = new RegexMapTestPanel(getMetadata());
            JOptionPane.showMessageDialog(panel, regexMapTestPanel.getPanel(), "Test metadata configuration", JOptionPane.PLAIN_MESSAGE);
        });
    }

    public MetadataPanel(Collection<MetadataMap> metadata) {
        this();

        metadata.forEach(this::addMetadata);
    }

    private void createUIComponents() {
        panelMetadata = new JPanel();
        panelMetadata.setLayout(new BoxLayout(panelMetadata, BoxLayout.PAGE_AXIS));
    }

    public JPanel getPanel() {
        return panel;
    }

    private void addMetadata(MetadataMap metadataMap) {
        MetadataOverviewPanel metadataOverviewPanel = new MetadataOverviewPanel(this, metadataMap);
        metadataOverviewPanel.getPanel().setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMetadata.add(metadataOverviewPanel.getPanel());

        metadataMaps.put(metadataMap, metadataOverviewPanel);

        setChanged();
        notifyObservers(metadataMap);
    }

    void removeMetadata(MetadataMap metadataMap) {
        panelMetadata.remove(metadataMaps.get(metadataMap).getPanel());
        metadataMaps.remove(metadataMap);

        setChanged();
        notifyObservers();
    }

    void editMetadata(MetadataMap metadataMap) {
        MetadataOverviewPanel metadataOverviewPanel = metadataMaps.remove(metadataMap);
        metadataMaps.put(metadataMap, metadataOverviewPanel);

        setChanged();
        notifyObservers(metadataMap);
    }

    public Collection<MetadataMap> getMetadata() {
        return metadataMaps.keySet();
    }

    public void addConfigurationChangeListener(ConfigurationChangeListener listener) {
        addObserver(listener);
    }
}
