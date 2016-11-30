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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PropertySelectionPanel {

    private JPanel panel;

    private JComboBox<String> comboBoxInstrument;
    private JComboBox<String> comboBoxExternal;
    private JComboBox<PropertyComboBoxItem> comboBoxProperty;

    private JButton buttonSearchSettings;

    public PropertySelectionPanel() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));

        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panel.add(panelTop);
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panel.add(panelBottom);

        JLabel labelInstrument = new JLabel("Instrument");
        labelInstrument.setPreferredSize(new Dimension(80, 25));
        panelTop.add(labelInstrument);
        comboBoxInstrument = new JComboBox<>();
        comboBoxInstrument.setPreferredSize(new Dimension(250, 25));
        comboBoxInstrument.setMaximumSize(new Dimension(250, 25));
        panelTop.add(comboBoxInstrument);

        JLabel labelProperty = new JLabel("Property");
        panelTop.add(labelProperty);
        ComboBoxModel<PropertyComboBoxItem> sortedComboBoxModel = new SortedComboBoxModel<>();
        comboBoxProperty = new JComboBox<>(sortedComboBoxModel);
        comboBoxProperty.setActionCommand("propertyChanged");
        comboBoxProperty.setPreferredSize(new Dimension(450, 25));
        comboBoxProperty.setMaximumSize(new Dimension(450, 25));
        panelTop.add(comboBoxProperty);

        buttonSearchSettings = new JButton(new ImageIcon(getClass().getResource("/images/search.png")));
        buttonSearchSettings.setToolTipText("advanced search settings");
        panelTop.add(buttonSearchSettings);

        JLabel labelExternal = new JLabel("Temperature");
        labelExternal.setPreferredSize(new Dimension(80, 25));
        panelBottom.add(labelExternal);
        comboBoxExternal = new JComboBox<>();
        comboBoxExternal.setPreferredSize(new Dimension(250, 25));
        comboBoxExternal.setMaximumSize(new Dimension(250, 25));
        panelBottom.add(comboBoxExternal);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void addInstrument(String instrument) {
        comboBoxInstrument.addItem(instrument);
    }

    public void addExternalInstrument(String instrument) {
        comboBoxExternal.addItem(instrument);
    }

    public void addProperty(String propertyName, String propertyAccession) {
        comboBoxProperty.setActionCommand("ignore");
        comboBoxProperty.addItem(new PropertyComboBoxItem(propertyName, propertyAccession));
        comboBoxProperty.setActionCommand("propertyChanged");
    }

    public String getSelectedInstrument() {
        return (String) comboBoxInstrument.getSelectedItem();
    }

    public String getSelectedExternalInstrument() {
        return (String) comboBoxExternal.getSelectedItem();
    }

    public String getSelectedPropertyName() {
        return ((PropertyComboBoxItem) comboBoxProperty.getSelectedItem()).getName();
    }

    public String getSelectedPropertyAccession() {
        return ((PropertyComboBoxItem) comboBoxProperty.getSelectedItem()).getAccession();
    }

    public boolean hasValidPropertyIndex() {
        return comboBoxProperty.getSelectedIndex() >= 0 &&
                comboBoxProperty.getSelectedIndex() < comboBoxProperty.getItemCount();
    }

    public boolean hasNext() {
        return comboBoxProperty.getSelectedIndex() < comboBoxProperty.getItemCount() - 1;
    }

    public boolean hasPrevious() {
        return comboBoxProperty.getSelectedIndex() > 0;
    }

    public void advanceProperty(boolean forward) {
        if(forward && hasNext()) {
            comboBoxProperty.setSelectedIndex(comboBoxProperty.getSelectedIndex() + 1);
        } else if(!forward && hasPrevious()) {
            comboBoxProperty.setSelectedIndex(comboBoxProperty.getSelectedIndex() - 1);
        }
    }

    public void initializeEmptyProperty() {
        comboBoxProperty.setSelectedIndex(-1);
    }

    public void clearInstruments() {
        comboBoxInstrument.removeAllItems();
        comboBoxExternal.removeAllItems();
    }

    public void clearProperties() {
        comboBoxProperty.removeAllItems();
    }

    public void addInstrumentChangeListener(ActionListener listener) {
        comboBoxInstrument.addActionListener(listener);
    }

    public void addExternalInstrumentChangeListener(ActionListener listener) {
        comboBoxExternal.addActionListener(listener);
    }

    public void addPropertyChangeListener(ActionListener listener) {
        comboBoxProperty.addActionListener(listener);
    }

    public void addAdvancedSearchListener(ActionListener listener) {
        buttonSearchSettings.addActionListener(listener);
    }
}
