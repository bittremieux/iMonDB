package inspector.jmondb.viewer.view.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PropertySelectionPanel {

    private JPanel panel;

    private JComboBox<String> comboBoxInstrument;
    private JComboBox<PropertyComboBoxItem> comboBoxProperty;

    private JButton buttonSearchSettings;

    public PropertySelectionPanel() {
        panel = new JPanel();

        JLabel labelInstrument = new JLabel("Instrument");
        panel.add(labelInstrument);
        comboBoxInstrument = new JComboBox<>();
        comboBoxInstrument.setPreferredSize(new Dimension(250, 25));
        comboBoxInstrument.setMaximumSize(new Dimension(250, 25));
        panel.add(comboBoxInstrument);

        JLabel labelProperty = new JLabel("Property");
        panel.add(labelProperty);
        ComboBoxModel<PropertyComboBoxItem> sortedComboBoxModel = new SortedComboBoxModel<>();
        comboBoxProperty = new JComboBox<>(sortedComboBoxModel);
        comboBoxProperty.setPreferredSize(new Dimension(450, 25));
        comboBoxProperty.setMaximumSize(new Dimension(450, 25));
        panel.add(comboBoxProperty);

        buttonSearchSettings = new JButton(new ImageIcon(getClass().getResource("/images/search.png")));
        buttonSearchSettings.setToolTipText("advanced search settings");
        panel.add(buttonSearchSettings);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void addInstrument(String instrument) {
        comboBoxInstrument.addItem(instrument);
    }

    public void addProperty(String propertyName, String propertyAccession) {
        comboBoxProperty.addItem(new PropertyComboBoxItem(propertyName, propertyAccession));
    }

    public String getSelectedInstrument() {
        return (String) comboBoxInstrument.getSelectedItem();
    }

    public String getSelectedPropertyName() {
        return ((PropertyComboBoxItem) comboBoxProperty.getSelectedItem()).getName();
    }

    public String getSelectedPropertyAccession() {
        return ((PropertyComboBoxItem) comboBoxProperty.getSelectedItem()).getAccession();
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

    public void clearInstruments() {
        comboBoxInstrument.removeAllItems();
    }

    public void clearProperties() {
        comboBoxProperty.removeAllItems();
    }

    public void addInstrumentChangeListener(ActionListener listener) {
        comboBoxInstrument.addActionListener(listener);
    }

    public void addPropertyChangeListener(ActionListener listener) {
        comboBoxProperty.addActionListener(listener);
    }

    public void addAdvancedSearchListener(ActionListener listener) {
        buttonSearchSettings.addActionListener(listener);
    }
}
