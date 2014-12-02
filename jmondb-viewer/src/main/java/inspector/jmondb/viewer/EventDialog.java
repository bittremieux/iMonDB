package inspector.jmondb.viewer;

/*
 * #%L
 * jMonDB Viewer
 * %%
 * Copyright (C) 2014 InSPECtor
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

import inspector.jmondb.model.EventType;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import inspector.jmondb.model.Event;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;

public class EventDialog extends JPanel {

	private static final Logger logger = LogManager.getLogger(EventDialog.class);

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

	public EventDialog(JComboBox<String> instruments) {
		setLayout(new BorderLayout());

		JPanel panelTop = new JPanel(new FlowLayout( FlowLayout.CENTER, 25, 5));
		add(panelTop, BorderLayout.PAGE_START);
		JPanel panelCenter = new JPanel();
		add(panelCenter, BorderLayout.CENTER);
		JPanel panelBottom = new JPanel();
		add(panelBottom, BorderLayout.PAGE_END);

		// instrument name
		JPanel panelInstrument = new JPanel(new BorderLayout());
		JLabel labelInstrument = new JLabel("Instrument name", JLabel.CENTER);
		panelInstrument.add(labelInstrument, BorderLayout.PAGE_START);
		comboBoxInstrument = new JComboBox<>();
		comboBoxInstrument.setPreferredSize(new Dimension(200, 25));
		// add all instrument names
		// this has to be a different combobox because we don't want our choices here to influence the general application
		for(int i = 0; i < instruments.getItemCount(); i++)
			comboBoxInstrument.addItem(instruments.getItemAt(i));
		// select the previously selected instrument
		comboBoxInstrument.setSelectedIndex(instruments.getSelectedIndex());
		panelInstrument.add(comboBoxInstrument, BorderLayout.CENTER);
		panelTop.add(panelInstrument);

		// date
		JPanel panelDate = new JPanel(new BorderLayout());
		JLabel labelDate = new JLabel("Date", JLabel.CENTER);
		panelDate.add(labelDate, BorderLayout.PAGE_START);
		model = new UtilDateModel();
		JDatePanelImpl datePanel = new JDatePanelImpl(model);
		datePicker = new JDatePickerDisableImpl(datePanel, new JFormattedTextField.AbstractFormatter() {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			@Override
			public Object stringToValue(String text) throws ParseException {
				return sdf.parse(text);
			}

			@Override
			public String valueToString(Object value) throws ParseException {
				if (value != null) {
					Calendar cal = (Calendar) value;
					return sdf.format(cal.getTime());
				}
				else
					return "";
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
		for(EventType type : EventType.values())
			comboBoxType.addItem(type);
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
		labelAttachmentName = new JLabel("No attachment added", new ImageIcon(getClass().getResource("/images/no-file.png")), SwingConstants.CENTER);
		labelAttachmentName.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2 && attachmentContent != null) {
					Thread fileOpener = new Thread() {
						public void run() {
							try {
								// store the attachment in a temporary file
								String prefix = FilenameUtils.getBaseName(getAttachmentName()) + "_";
								String suffix = "." + FilenameUtils.getExtension(getAttachmentName());
								File temp = File.createTempFile(prefix, suffix);
								temp.deleteOnExit();
								FileUtils.writeByteArrayToFile(temp, attachmentContent);

								// open the attachment in the default application
								Desktop.getDesktop().open(temp);
							} catch(IOException e1) {
								logger.warn("Error while saving attachment <{}> to a temporary file", getAttachmentName());
							}
						}
					};
					fileOpener.start();
				}
			}
		});
		panelBottom.add(labelAttachmentName);

		JButton buttonAdd = new JButton(new ImageIcon(getClass().getResource("/images/folder.png")));
		buttonAdd.addActionListener(new ListenerAddAttachment());
		panelBottom.add(buttonAdd);

		JButton buttonDelete = new JButton(new ImageIcon(getClass().getResource("/images/garbage.png")));
		buttonDelete.addActionListener(new ListenerRemoveAttachment());
		panelBottom.add(buttonDelete);

		JButton buttonSave = new JButton(new ImageIcon(getClass().getResource("/images/save.png")));
		buttonSave.addActionListener(new ListenerSaveAttachment());
		panelBottom.add(buttonSave);
	}

	public EventDialog(JComboBox<String> instruments, Event event) {
		this(instruments);

		// copy information of given event
		comboBoxInstrument.setSelectedItem(event.getInstrument().getName());
		comboBoxInstrument.setEnabled(false);
		model.setValue(new Date(event.getDate().getTime()));
		model.setSelected(true);
		datePicker.setButtonEnabled(false);
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
		}
		else
			return null;
	}

	public EventType getType() {
		return (EventType) comboBoxType.getSelectedItem();
	}

	public String getProblem() {
		String text = textProblem.getText();
		if(!text.equals(""))
			return text;
		else
			return null;
	}

	public String getSolution() {
		String text = textSolution.getText();
		if(!text.equals(""))
			return text;
		else
			return null;
	}

	public String getExtra() {
		String text = textExtra.getText();
		if(!text.equals(""))
			return text;
		else
			return null;
	}

	public String getAttachmentName() {
		String text = labelAttachmentName.getText();
		if(!"No attachment added".equals(text))
			return text;
		else
			return null;
	}

	public byte[] getAttachmentContent() {
		return attachmentContent;
	}

	private void setAttachmentIconFileType() {
		if(attachmentContent != null) {
			String type = new Tika().detect(attachmentContent);

			if(type.contains("audio"))
				labelAttachmentName.setIcon(new ImageIcon(getClass().getResource("/images/audio.png")));
			else if(type.contains("image"))
				labelAttachmentName.setIcon(new ImageIcon(getClass().getResource("/images/image.png")));
			else if(type.contains("text"))
				labelAttachmentName.setIcon(new ImageIcon(getClass().getResource("/images/text.png")));
			else if(type.contains("video"))
				labelAttachmentName.setIcon(new ImageIcon(getClass().getResource("/images/video.png")));
			else
				labelAttachmentName.setIcon(new ImageIcon(getClass().getResource("/images/general-file.png")));
		}
		else
			labelAttachmentName.setIcon(new ImageIcon(getClass().getResource("/images/no-file.png")));
	}

	private class ListenerAddAttachment implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();

			int returnVal = fileChooser.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				File attachment = fileChooser.getSelectedFile();
				// file name
				labelAttachmentName.setText(attachment.getName());

				logger.trace("Add file <{}> as attachment", attachment.getName());

				// file content
				Thread attachmentReader = new Thread() {
					public void run() {
						try {
							attachmentContent = FileUtils.readFileToByteArray(attachment);
							// set the icon according to the file type
							setAttachmentIconFileType();
						} catch(IOException e1) {
							logger.warn("Error while adding attachment <{}>: {}", attachment.getName(), e1.getMessage());
						}
					}
				};
				attachmentReader.start();
			}
		}
	}

	private class ListenerRemoveAttachment implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if(attachmentContent != null) {
				logger.trace("Remove attachment <{}>", getAttachmentName());

				labelAttachmentName.setText("No attachment added");
				labelAttachmentName.setIcon(new ImageIcon(getClass().getResource("/images/no-file.png")));

				attachmentContent =  null;
			}
		}
	}

	private class ListenerSaveAttachment implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if(attachmentContent != null) {
				// show save dialog
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setSelectedFile(new File(getAttachmentName()));

				int returnVal = fileChooser.showSaveDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					// save the attachment to the selected file
					try {
						logger.trace("Save the attachment to file <{}>", fileChooser.getSelectedFile().getPath());
						FileUtils.writeByteArrayToFile(fileChooser.getSelectedFile(), attachmentContent);
					} catch(IOException e1) {
						logger.warn("Error while saving the attachment to file <{}>: {}", fileChooser.getSelectedFile().getPath(), e1.getMessage());
						JOptionPane.showMessageDialog(null, "Could not save the attachment", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else {
				logger.trace("No attachment to be saved");
			}
		}
	}
}
