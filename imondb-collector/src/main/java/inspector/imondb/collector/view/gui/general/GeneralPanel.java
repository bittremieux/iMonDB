package inspector.imondb.collector.view.gui.general;

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
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class GeneralPanel {

    private JPanel panel;

    private JLabel labelDirectory;
    private JButton buttonDirectory;

    private JTextField textFieldRegex;
    private JButton buttonRegex;

    private UtilDateModel model;
    private JDatePanelImpl datePanel;
    private JPanel panelDate;

    private JSpinner spinnerThreads;

    private JCheckBox checkBoxUnique;

    public GeneralPanel() {
        buttonRegex.addActionListener(e -> {
            RegexTestPanel testPanel = new RegexTestPanel(textFieldRegex.getText());
            JOptionPane.showMessageDialog(panel, testPanel.getPanel(), "Test file filter", JOptionPane.PLAIN_MESSAGE);
            textFieldRegex.setText(testPanel.getRegex());
        });

        model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new JFormattedTextField.AbstractFormatter() {
            private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            public Object stringToValue(String text) throws ParseException {
                return sdf.parse(text);
            }

            @Override
            public String valueToString(Object value) throws ParseException {
                return value != null ? sdf.format(((Calendar) value).getTime()) : "";
            }
        });
        panelDate.add(datePicker);

        ((DefaultFormatter) ((JSpinner.DefaultEditor) spinnerThreads.getEditor()).getTextField().getFormatter()).setCommitsOnValidEdit(true);

        buttonDirectory.setName("gen_dir");
        textFieldRegex.setName("gen_regex");
        datePanel.setName("gen_date");
        spinnerThreads.setName("gen_threads");
        checkBoxUnique.setName("gen_unique");
    }

    public GeneralPanel(String directory, String regex, Timestamp date, int threads, boolean unique) {
        this();

        if(directory != null) {
            setDirectory(directory);
        }
        textFieldRegex.setText(regex);
        if(date != null) {
            model.setValue(new Date(date.getTime()));
        }
        spinnerThreads.setValue(threads);
        checkBoxUnique.setSelected(unique);
    }

    private void createUIComponents() {
        int processors = Runtime.getRuntime().availableProcessors();
        spinnerThreads = new JSpinner(new SpinnerNumberModel(1, 1, processors, 1));
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) spinnerThreads.getEditor();
        spinnerEditor.getTextField().setHorizontalAlignment(JTextField.LEADING);
    }

    public void setDirectory(String dir) {
        labelDirectory.setText(shortenPath(dir));
        labelDirectory.setIcon(null);
        labelDirectory.setToolTipText(dir);
    }

    public String shortenPath(String path) {
        String pattern = "^[^/]*(/[^/]+/[^/]+/).*(/[^/]+/[^/]+/?)$";
        String replacement = "$1...$2";
        return path.matches(pattern) ? path.replaceFirst(pattern, replacement) : path;
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getDirectory() {
        if(!"<html><i>no directory configured</i></html>".equals(labelDirectory.getText())) {
            return labelDirectory.getToolTipText();
        } else {
            return null;
        }
    }

    public String getFileNameRegex() {
        return textFieldRegex.getText();
    }

    public Timestamp getStartDate() {
        return model.getValue() != null ? new Timestamp(model.getValue().getTime()) : null;
    }

    public int getNumberOfThreads() {
        return (int) spinnerThreads.getValue();
    }

    public boolean getEnforceUnique() {
        return checkBoxUnique.isSelected();
    }

    public void addConfigurationChangeListener(ConfigurationChangeListener listener) {
        buttonDirectory.addActionListener(listener);
        textFieldRegex.addFocusListener(listener);
        datePanel.addActionListener(listener);
        spinnerThreads.addChangeListener(listener);
        checkBoxUnique.addActionListener(listener);
    }
}
