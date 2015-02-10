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

import inspector.imondb.model.Metadata;
import inspector.imondb.viewer.viewmodel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class SearchDialog {

    private static ImageIcon iconAdd = new ImageIcon(SearchDialog.class.getResource("/images/add.png"));
    private static ImageIcon iconRemove = new ImageIcon(SearchDialog.class.getResource("/images/remove.png"));

	private JPanel panel;

    private JTextField filterTextField;

    private List<JComboBox> comboBoxes;

    private SortedSet<String> keys;
    private SortedSet<String> values;

    private JPanel metadataPanel;
    private JScrollPane metadataScrollPane;

    public SearchDialog(List<Metadata> metadata) {
        comboBoxes = new ArrayList<>();

        keys = new TreeSet<>();
        values = new TreeSet<>();
        for(Metadata md : metadata) {
            keys.add(md.getName());
            values.add(md.getValue());
        }

		panel = new JPanel();
        panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(650, 400));

        // properties filter
        JPanel filterPropertiesPanel = new JPanel();
        JLabel filterLabel = new JLabel("Filter properties on name:");
        filterPropertiesPanel.add(filterLabel);
        filterTextField = new JTextField();
        filterTextField.setPreferredSize(new Dimension(250, 25));
        filterPropertiesPanel.add(filterTextField);

        // metadata filter
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel addMetadataPanel = new JPanel();
        JLabel labelAdd = new JLabel("Specify additional metadata search settings");
        addMetadataPanel.add(labelAdd);
        JButton buttonAdd = new JButton(iconAdd);
        if(metadata.size() == 0) {
			buttonAdd.setEnabled(false);
		}
        buttonAdd.addActionListener(new AddListener());
        addMetadataPanel.add(buttonAdd);
        centerPanel.add(addMetadataPanel, BorderLayout.PAGE_START);

        metadataPanel = new JPanel(new GridLayout(0, 1));
        metadataScrollPane = new JScrollPane(metadataPanel);
        centerPanel.add(metadataScrollPane, BorderLayout.CENTER);

		panel.add(filterPropertiesPanel, BorderLayout.PAGE_START);
		panel.add(centerPanel, BorderLayout.CENTER);
    }

	public SearchDialog(List<Metadata> metadata, String propertyFilter, MetadataFilter metadataFilter) {
		this(metadata);

		filterTextField.setText(propertyFilter);

        if(metadataFilter != null) {
            for(Object elem : metadataFilter) {
                if(elem instanceof MetadataEntry) {
                    MetadataEntry entry = (MetadataEntry) elem;
                    addMetadataPanel(entry.getKey(), entry.getOperator(), entry.getValue());
                } else if(elem instanceof MetadataConnector) {
                    addConnectorPanel((MetadataConnector) elem);
                }
            }
        }
	}

    private void addMetadataPanel(String key, MetadataOperator operator, String value) {
        JPanel metadataPanel = new JPanel();
        JComboBox<String> comboBoxKey = new JComboBox<>(keys.toArray(new String[keys.size()]));
        if(key != null) {
            comboBoxKey.setSelectedItem(key);
        }
        comboBoxKey.setPreferredSize(new Dimension(250, 25));
        metadataPanel.add(comboBoxKey);

        JComboBox<MetadataOperator> comboBoxOperator = new JComboBox<>(
                new MetadataOperator[] { MetadataOperator.EQUAL, MetadataOperator.NOT_EQUAL });
        if(operator != null) {
            comboBoxOperator.setSelectedItem(operator);
        }
        metadataPanel.add(comboBoxOperator);

        JComboBox<String> comboBoxValue = new JComboBox<>(values.toArray(new String[values.size()]));
        if(value != null) {
            comboBoxValue.setSelectedItem(value);
        }
        comboBoxValue.setPreferredSize(new Dimension(250, 25));
        metadataPanel.add(comboBoxValue);

        JButton buttonRemove = new JButton(iconRemove);
        buttonRemove.addActionListener(new RemoveListener());
        metadataPanel.add(buttonRemove);

        metadataPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        comboBoxes.add(comboBoxKey);
        comboBoxes.add(comboBoxOperator);
        comboBoxes.add(comboBoxValue);
        this.metadataPanel.add(metadataPanel);
    }

    private void addConnectorPanel(MetadataConnector connector) {
        JPanel panel = new JPanel();
        JComboBox<MetadataConnector> comboBox = new JComboBox<>(
                new MetadataConnector[] { MetadataConnector.AND, MetadataConnector.OR });
        if(connector != null) {
            comboBox.setSelectedItem(connector);
        }
        panel.add(comboBox);

        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        comboBoxes.add(comboBox);
        metadataPanel.add(panel);
    }

	public JPanel getPanel() {
		return panel;
	}

    public String getPropertyFilter() {
        return filterTextField.getText().isEmpty() ? null : filterTextField.getText();
    }

    public MetadataFilter getMetadata() {
        if(comboBoxes.size() == 0) {
            return null;
        } else {
            MetadataFilter metadata = new MetadataFilter();

            int index = 0;
            while(index < comboBoxes.size()) {
                if(index % 4 == 0) {
                    metadata.add(new MetadataEntry((String) comboBoxes.get(index).getSelectedItem(),
                            (MetadataOperator) comboBoxes.get(index + 1).getSelectedItem(),
                            (String) comboBoxes.get(index + 2).getSelectedItem()));
                    index += 3;
                } else if(index % 4 == 3) {
                    metadata.add(comboBoxes.get(index).getSelectedItem());
                    index++;
                }
            }

            return metadata;
        }
    }

    private class AddListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(metadataPanel.getComponentCount() > 0) {
				addConnectorPanel(null);
            }

            addMetadataPanel(null, null, null);

            metadataPanel.validate();
            metadataPanel.repaint();
            metadataScrollPane.validate();
        }
    }

    private class RemoveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // find the components that need to be deleted
            int componentIndex = getComponentIndex(((JComponent) e.getSource()).getParent());
            int componentFromIndex = componentIndex, componentToIndex = componentIndex + 1;
            int listFromIndex = (componentIndex / 2) * 4, listToIndex = (componentIndex / 2) * 4 + 3;
            if(componentIndex == 0 && metadataPanel.getComponentCount() > 1) {
                // first element with subsequent elements -> remove the subsequent connector
                componentToIndex++;
                listToIndex++;
            } else if(componentIndex == metadataPanel.getComponentCount() - 1 && metadataPanel.getComponentCount() > 1) {
                // last element with prior elements -> remove the previous connector
                componentFromIndex--;
                listFromIndex--;
            } else if(componentIndex > 0 && componentIndex < metadataPanel.getComponentCount()) {
                // element in the middle -> remove the subsequent connector
                componentToIndex++;
                listToIndex++;
            }
            // remove visual components and combobox information
            for(int i = componentFromIndex; i < componentToIndex; i++) {
                metadataPanel.remove(componentFromIndex);
            }
            comboBoxes.subList(listFromIndex, listToIndex).clear();

            metadataPanel.validate();
            metadataPanel.repaint();
            metadataScrollPane.validate();
        }

        private int getComponentIndex(Component component) {
            if(component != null && component.getParent() != null) {
                Container c = component.getParent();
                for(int i = 0; i < c.getComponentCount(); i++) {
                    if(c.getComponent(i) == component) {
                        return i;
                    }
                }
            }

            return -1;
        }
    }
}
