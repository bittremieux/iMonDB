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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import inspector.imondb.collector.controller.listeners.ConfigurationChangeListener;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
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
        $$$setupUI$$$();
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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
        panel.add(panel1);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(11);
        label1.setText("<html><b>Directory</b></html>");
        label1.setToolTipText("The start directory containing child directories (or raw files directly) that will be processed");
        CellConstraints cc = new CellConstraints();
        panel1.add(label1, cc.xy(1, 1));
        final JLabel label2 = new JLabel();
        label2.setHorizontalAlignment(11);
        label2.setText("<html><b>File name regex</b></html>");
        label2.setDisplayedMnemonic('R');
        label2.setDisplayedMnemonicIndex(19);
        label2.setToolTipText("A regular expression used to match the file name of the raw files that need to be processed");
        panel1.add(label2, cc.xy(1, 3));
        final JLabel label3 = new JLabel();
        label3.setHorizontalAlignment(11);
        label3.setText("Starting date");
        label3.setToolTipText("Only files with a modification date later then the starting date will be processed");
        panel1.add(label3, cc.xy(1, 5));
        final JLabel label4 = new JLabel();
        label4.setHorizontalAlignment(11);
        label4.setText("<html>Number of threads</html>");
        label4.setDisplayedMnemonic('T');
        label4.setDisplayedMnemonicIndex(16);
        label4.setToolTipText("The number of worker threads used for collecting the raw files and processing them");
        panel1.add(label4, cc.xy(1, 7));
        panel1.add(spinnerThreads, cc.xy(3, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(1, 0));
        panel1.add(panel2, cc.xy(3, 1));
        labelDirectory = new JLabel();
        labelDirectory.setIcon(new ImageIcon(getClass().getResource("/images/nok.png")));
        labelDirectory.setInheritsPopupMenu(false);
        labelDirectory.setMaximumSize(new Dimension(350, 16));
        labelDirectory.setMinimumSize(new Dimension(350, 16));
        labelDirectory.setPreferredSize(new Dimension(350, 16));
        labelDirectory.setText("<html><i>no directory configured</i></html>");
        panel2.add(labelDirectory, BorderLayout.CENTER);
        buttonDirectory = new JButton();
        buttonDirectory.setHorizontalTextPosition(0);
        buttonDirectory.setIcon(new ImageIcon(getClass().getResource("/images/add.png")));
        buttonDirectory.setMaximumSize(new Dimension(24, 24));
        buttonDirectory.setMinimumSize(new Dimension(24, 24));
        buttonDirectory.setPreferredSize(new Dimension(24, 24));
        buttonDirectory.setText("");
        panel2.add(buttonDirectory, BorderLayout.EAST);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(1, 0));
        panel1.add(panel3, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        textFieldRegex = new JTextField();
        textFieldRegex.setText("^.*\\.raw$");
        panel3.add(textFieldRegex, BorderLayout.CENTER);
        buttonRegex = new JButton();
        buttonRegex.setHorizontalTextPosition(0);
        buttonRegex.setIcon(new ImageIcon(getClass().getResource("/images/search.png")));
        buttonRegex.setMaximumSize(new Dimension(24, 24));
        buttonRegex.setMinimumSize(new Dimension(24, 24));
        buttonRegex.setPreferredSize(new Dimension(24, 24));
        buttonRegex.setText("");
        panel3.add(buttonRegex, BorderLayout.EAST);
        panelDate = new JPanel();
        panelDate.setLayout(new BorderLayout(0, 0));
        panel1.add(panelDate, cc.xy(3, 5));
        checkBoxUnique = new JCheckBox();
        checkBoxUnique.setText("Enforce unique run names");
        checkBoxUnique.setMnemonic('U');
        checkBoxUnique.setDisplayedMnemonicIndex(8);
        panel1.add(checkBoxUnique, cc.xyw(1, 9, 3, CellConstraints.CENTER, CellConstraints.DEFAULT));
        label2.setLabelFor(textFieldRegex);
        label4.setLabelFor(spinnerThreads);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}
