package inspector.jmondb.viewer;

import inspector.jmondb.model.EventType;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import inspector.jmondb.model.Event;
import org.apache.commons.io.IOUtils;

public class EventDialog extends JPanel {

	private JComboBox<String> comboBoxInstrument;
	private UtilDateModel model;
	private JDatePickerImpl datePicker;
	private JComboBox<EventType> comboBoxType;
	private JTextField textFieldDescription;
	private byte[] picture;
	private JLabel pictureField;

	private static int MAX_HEIGHT = 100;
	private int MAX_WIDTH = 100;

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
		model = new UtilDateModel();
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
		pictureField = new JLabel();
		pictureField.setPreferredSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
		panelFileSelection.add(pictureField, BorderLayout.CENTER);
		JButton buttonLoadFile = new JButton(new ImageIcon(Viewer.class.getResource("/images/open.gif"), "load picture"));
		buttonLoadFile.addActionListener(new ListenerLoadFile());
		panelFileSelection.add(buttonLoadFile, BorderLayout.LINE_END);
		add(panelPicture);
	}

	public EventDialog(JComboBox<String> instruments, Event event) {
		this(instruments);

		// copy information of given event
		comboBoxInstrument.setSelectedItem(event.getInstrument().getName());
		comboBoxInstrument.setEnabled(false);
		model.setValue(new Date(event.getDate().getTime()));
		model.setSelected(true);
		datePicker.remove(1);	// ugly hack to remove the button
		comboBoxType.setSelectedItem(event.getType());
		comboBoxType.setEnabled(false);
		textFieldDescription.setText(event.getDescription());
		if(event.getPicture() != null) {
			picture = event.getPicture();
			pictureField.setIcon(byteArrayToScaledIcon(picture));
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

	public String getDescription() {
		String text = textFieldDescription.getText();
		if(!text.equals(""))
			return text;
		else
			return null;
	}

	public byte[] getPictureAsByteArray() {
		return picture;
	}

	private class ListenerLoadFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			FileFilter filter = new FileNameExtensionFilter("Image file", "jpg", "jpeg", "png");
			fileChooser.setFileFilter(filter);

			int returnVal = fileChooser.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					picture = IOUtils.toByteArray(new FileInputStream(fileChooser.getSelectedFile()));
				} catch(IOException e1) {
					JOptionPane.showMessageDialog(null, "Selected picture file not found", "Error", JOptionPane.ERROR_MESSAGE);
				}
				pictureField.setIcon(byteArrayToScaledIcon(picture));
			}

		}
	}

	private ImageIcon byteArrayToScaledIcon(byte[] arr) {
		try {
			Image img = ImageIO.read(new ByteArrayInputStream(arr));
			if(img.getWidth(null) > MAX_WIDTH || img.getHeight(null) > MAX_HEIGHT)
				img = img.getScaledInstance(MAX_WIDTH, MAX_HEIGHT, Image.SCALE_SMOOTH);

			return new ImageIcon(img);
		} catch(IOException e1) {
			JOptionPane.showMessageDialog(null, "Error while reading the selected picture file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
}
