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

import inspector.imondb.model.Event;
import inspector.imondb.model.EventType;
import inspector.imondb.viewer.controller.listeners.AttachmentAddListener;
import inspector.imondb.viewer.controller.listeners.AttachmentOpenListener;
import inspector.imondb.viewer.controller.listeners.AttachmentRemoveListener;
import inspector.imondb.viewer.controller.listeners.AttachmentSaveListener;
import org.apache.tika.Tika;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EventDialog {

    private static Map<String, Icon> iconFileTypes = initializeIconFileTypes();

    private JPanel panel;

    // instrument name
    private JComboBox<String> comboBoxInstrument;

    // event date
    private UtilDateModel model;
    private JDatePickerDisableImpl datePicker;

    // event type
    private JComboBox<EventType> comboBoxType;

    // textual information
    private JTextArea textProblem;
    private JTextArea textSolution;
    private JTextArea textExtra;

    // attachment information
    private JLabel labelAttachmentName;
    private byte[] attachmentContent;

    public EventDialog(String instrument) {
        panel = new JPanel(new BorderLayout());

        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 5));
        panel.add(panelTop, BorderLayout.PAGE_START);
        JPanel panelCenter = new JPanel();
        panel.add(panelCenter, BorderLayout.CENTER);
        JPanel panelBottom = new JPanel();
        panel.add(panelBottom, BorderLayout.PAGE_END);

        // instrument name
        JPanel panelInstrument = new JPanel(new BorderLayout());
        JLabel labelInstrument = new JLabel("Instrument name", JLabel.CENTER);
        panelInstrument.add(labelInstrument, BorderLayout.PAGE_START);
        comboBoxInstrument = new JComboBox<>();
        comboBoxInstrument.setPreferredSize(new Dimension(200, 25));
        comboBoxInstrument.setEnabled(false);
        comboBoxInstrument.addItem(instrument);
        panelInstrument.add(comboBoxInstrument, BorderLayout.CENTER);
        panelTop.add(panelInstrument);

        // date
        JPanel panelDate = new JPanel(new BorderLayout());
        JLabel labelDate = new JLabel("Date", JLabel.CENTER);
        panelDate.add(labelDate, BorderLayout.PAGE_START);
        model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        datePicker = new JDatePickerDisableImpl(datePanel, new JFormattedTextField.AbstractFormatter() {
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
        panelDate.add(datePicker, BorderLayout.CENTER);
        panelTop.add(panelDate);

        // event type
        JPanel panelType = new JPanel(new BorderLayout());
        JLabel labelType = new JLabel("Event type", JLabel.CENTER);
        panelType.add(labelType, BorderLayout.PAGE_START);
        comboBoxType = new JComboBox<>();
        comboBoxType.setPreferredSize(new Dimension(200, 25));
        for(EventType type : EventType.values()) {
            comboBoxType.addItem(type);
        }
        panelType.add(comboBoxType, BorderLayout.CENTER);
        panelTop.add(panelType);

        // input text fields
        JPanel panelProblem = new JPanel(new BorderLayout());
        JLabel labelProblem = new JLabel("Problem", JLabel.CENTER);
        panelProblem.add(labelProblem, BorderLayout.PAGE_START);
        textProblem = new JTextArea();
        textProblem.setLineWrap(true);
        textProblem.setWrapStyleWord(true);
        JScrollPane problemScrollPane = new JScrollPane(textProblem);
        problemScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        problemScrollPane.setPreferredSize(new Dimension(300, 200));
        panelProblem.add(problemScrollPane, BorderLayout.CENTER);
        panelCenter.add(panelProblem);

        JPanel panelSolution = new JPanel(new BorderLayout());
        JLabel labelSolution = new JLabel("Solution", JLabel.CENTER);
        panelSolution.add(labelSolution, BorderLayout.PAGE_START);
        textSolution = new JTextArea();
        textSolution.setLineWrap(true);
        textSolution.setWrapStyleWord(true);
        JScrollPane solutionScrollPane = new JScrollPane(textSolution);
        solutionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        solutionScrollPane.setPreferredSize(new Dimension(300, 200));
        panelSolution.add(solutionScrollPane, BorderLayout.CENTER);
        panelCenter.add(panelSolution);

        JPanel panelExtra = new JPanel(new BorderLayout());
        JLabel labelExtra = new JLabel("Additional information", JLabel.CENTER);
        panelExtra.add(labelExtra, BorderLayout.PAGE_START);
        textExtra = new JTextArea();
        textExtra.setLineWrap(true);
        textExtra.setWrapStyleWord(true);
        JScrollPane extraScrollPane = new JScrollPane(textExtra);
        extraScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        extraScrollPane.setPreferredSize(new Dimension(300, 200));
        panelExtra.add(extraScrollPane, BorderLayout.CENTER);
        panelCenter.add(panelExtra);

        // attachment information
        JLabel labelAttachment = new JLabel("Attachment: ", JLabel.TRAILING);
        panelBottom.add(labelAttachment);
        labelAttachmentName = new JLabel("No attachment added", iconFileTypes.get("no-file"), SwingConstants.CENTER);
        labelAttachmentName.addMouseListener(new AttachmentOpenListener(this));
        panelBottom.add(labelAttachmentName);

        JButton buttonAdd = new JButton(new ImageIcon(getClass().getResource("/images/folder.png")));
        buttonAdd.addActionListener(new AttachmentAddListener(this));
        panelBottom.add(buttonAdd);

        JButton buttonDelete = new JButton(new ImageIcon(getClass().getResource("/images/garbage.png")));
        buttonDelete.addActionListener(new AttachmentRemoveListener(this));
        panelBottom.add(buttonDelete);

        JButton buttonSave = new JButton(new ImageIcon(getClass().getResource("/images/save.png")));
        buttonSave.addActionListener(new AttachmentSaveListener(this));
        panelBottom.add(buttonSave);
    }

    public EventDialog(String selectedInstrument, Event event) {
        this(selectedInstrument);

        // copy information of given event
        comboBoxInstrument.setSelectedItem(event.getInstrument().getName());
        model.setValue(new Date(event.getDate().getTime()));
        model.setSelected(true);
        datePicker.setEnabled(false);
        comboBoxType.setSelectedItem(event.getType());
        comboBoxType.setEnabled(false);
        textProblem.setText(event.getProblem());
        textSolution.setText(event.getSolution());
        textExtra.setText(event.getExtra());
        if(event.getAttachmentName() != null && event.getAttachmentContent() != null) {
            labelAttachmentName.setText(event.getAttachmentName());
            attachmentContent = event.getAttachmentContent();
            setAttachmentIconFileType();
        }
    }

    private static Map<String, Icon> initializeIconFileTypes() {
        Map<String, Icon> icons = new HashMap<>();
        icons.put("audio", new ImageIcon(EventDialog.class.getResource("/images/audio.png")));
        icons.put("image", new ImageIcon(EventDialog.class.getResource("/images/image.png")));
        icons.put("text", new ImageIcon(EventDialog.class.getResource("/images/text.png")));
        icons.put("video", new ImageIcon(EventDialog.class.getResource("/images/video.png")));
        icons.put("general-file", new ImageIcon(EventDialog.class.getResource("/images/general-file.png")));
        icons.put("no-file", new ImageIcon(EventDialog.class.getResource("/images/no-file.png")));

        return icons;
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getInstrumentName() {
        return (String) comboBoxInstrument.getSelectedItem();
    }

    public Timestamp getDate() {
        Date date = (Date) datePicker.getModel().getValue();
        if(date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return new Timestamp(cal.getTime().getTime());
        } else {
            return null;
        }
    }

    public EventType getType() {
        return (EventType) comboBoxType.getSelectedItem();
    }

    public String getProblem() {
        String text = textProblem.getText();
        if(!text.isEmpty()) {
            return text;
        } else {
            return null;
        }
    }

    public String getSolution() {
        String text = textSolution.getText();
        if(!text.isEmpty()) {
            return text;
        } else {
            return null;
        }
    }

    public String getExtra() {
        String text = textExtra.getText();
        if(!text.isEmpty()) {
            return text;
        } else {
            return null;
        }
    }

    public String getAttachmentName() {
        String text = labelAttachmentName.getText();
        if(!"No attachment added".equals(text)) {
            return text;
        } else {
            return null;
        }
    }

    public void setAttachmentName(String name) {
        labelAttachmentName.setText(name);
    }

    public byte[] getAttachmentContent() {
        return attachmentContent != null ? attachmentContent.clone() : null;
    }

    public void setAttachmentContent(byte[] attachment) {
        attachmentContent = attachment != null ? attachment.clone() : null;
    }

    public void setAttachmentIconFileType() {
        if(attachmentContent != null) {
            String type = new Tika().detect(attachmentContent);

            if(type.contains("audio")) {
                labelAttachmentName.setIcon(iconFileTypes.get("audio"));
            } else if(type.contains("image")) {
                labelAttachmentName.setIcon(iconFileTypes.get("image"));
            } else if(type.contains("text")) {
                labelAttachmentName.setIcon(iconFileTypes.get("text"));
            } else if(type.contains("video")) {
                labelAttachmentName.setIcon(iconFileTypes.get("video"));
            } else {
                labelAttachmentName.setIcon(iconFileTypes.get("general-file"));
            }
        } else {
            labelAttachmentName.setIcon(iconFileTypes.get("no-file"));
        }
    }
}
