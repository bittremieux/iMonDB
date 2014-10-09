package inspector.jmondb.viewer;

import inspector.jmondb.model.EventType;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EventDialog extends JPanel {

	private JComboBox<String> comboBoxInstrument;
	private JDatePickerImpl datePicker;
	private JComboBox<EventType> comboBoxType;
	private JTextField textFieldDescription;
	private File picture;
	private JLabel pictureName;

	public EventDialog(JComboBox<String> instruments) {
		setLayout(new GridLayout(5, 1));

		// instrument name
		JPanel panelInstrument = new JPanel(new GridLayout(1, 2));
		JLabel labelInstrument = new JLabel("Instrument name: ");
		panelInstrument.add(labelInstrument);
		comboBoxInstrument = new JComboBox<>();
		// add all instrument names
		for(int i = 0; i < instruments.getItemCount(); i++)
			comboBoxInstrument.addItem(instruments.getItemAt(i));
		// select the previously selected instrument
		comboBoxInstrument.setSelectedIndex(instruments.getSelectedIndex());
		panelInstrument.add(comboBoxInstrument);
		add(panelInstrument);

		// date
		JPanel panelDate = new JPanel(new GridLayout(1, 2));
		JLabel labelDate = new JLabel("Date:");
		panelDate.add(labelDate);
		UtilDateModel model = new UtilDateModel();
		JDatePanelImpl datePanel = new JDatePanelImpl(model);
		datePicker = new JDatePickerImpl(datePanel, new JFormattedTextField.AbstractFormatter() {
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
		panelDate.add(datePicker);
		add(panelDate);

		// event type
		JPanel panelType = new JPanel(new GridLayout(1, 2));
		JLabel labelType = new JLabel("Event type:");
		panelType.add(labelType);
		comboBoxType = new JComboBox<>();
		for(EventType type : EventType.values())
			comboBoxType.addItem(type);
		panelType.add(comboBoxType);
		add(panelType);

		// description
		JPanel panelDescription = new JPanel(new GridLayout(1, 2));
		JLabel labelDescription = new JLabel("Description: ");
		panelDescription.add(labelDescription);
		textFieldDescription = new JTextField();
		panelDescription.add(textFieldDescription);
		add(panelDescription);

		// picture attachment
		JPanel panelPicture = new JPanel(new GridLayout(1, 2));
		JLabel labelPicture = new JLabel("Picture: ");
		panelPicture.add(labelPicture);
		JPanel panelFileSelection = new JPanel(new BorderLayout());
		panelPicture.add(panelFileSelection);
		pictureName = new JLabel();
		panelFileSelection.add(pictureName, BorderLayout.CENTER);
		JButton buttonLoadFile = new JButton(new ImageIcon(Viewer.class.getResource("/images/open.gif"), "load picture"));
		buttonLoadFile.addActionListener(new ListenerLoadFile());
		panelFileSelection.add(buttonLoadFile, BorderLayout.LINE_END);
		add(panelPicture);
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

	public String getDescription() {
		String text = textFieldDescription.getText();
		if(!text.equals(""))
			return text;
		else
			return null;
	}

	public byte[] getPictureAsByteArray() throws IOException {
		if(picture != null)
			return Files.readAllBytes(Paths.get(picture.getPath()));
		else
			return null;
	}

	private class ListenerLoadFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			FileFilter filter = new FileNameExtensionFilter("Image file", "jpg", "jpeg", "png");
			fileChooser.setFileFilter(filter);

			int returnVal = fileChooser.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {

				picture = fileChooser.getSelectedFile();
				pictureName.setText(picture.getName());
			}

		}
	}
}
