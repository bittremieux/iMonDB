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
	private static int MAX_WIDTH = 150;

	public EventDialog(JComboBox<String> instruments) {
		setLayout(new SpringLayout());

		// instrument name
		JLabel labelInstrument = new JLabel("Instrument name: ");
		add(labelInstrument);
		comboBoxInstrument = new JComboBox<>();
		// add all instrument names
		for(int i = 0; i < instruments.getItemCount(); i++)
			comboBoxInstrument.addItem(instruments.getItemAt(i));
		// select the previously selected instrument
		comboBoxInstrument.setSelectedIndex(instruments.getSelectedIndex());
		add(comboBoxInstrument);

		// date
		JLabel labelDate = new JLabel("Date:");
		add(labelDate);
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
		add(datePicker);

		// event type
		JLabel labelType = new JLabel("Event type:");
		add(labelType);
		comboBoxType = new JComboBox<>();
		for(EventType type : EventType.values())
			comboBoxType.addItem(type);
		add(comboBoxType);

		// description
		JLabel labelDescription = new JLabel("Description: ");
		add(labelDescription);
		textFieldDescription = new JTextField();
		add(textFieldDescription);

		// picture attachment
		JLabel labelPicture = new JLabel("Picture: ");
		add(labelPicture);
		JPanel panelFileSelection = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		add(panelFileSelection);

		pictureField = new JLabel();
		pictureField.setHorizontalAlignment(SwingConstants.CENTER);
		pictureField.setVerticalAlignment(SwingConstants.CENTER);
		pictureField.setPreferredSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		panelFileSelection.add(pictureField, gbc);

		JPanel panelButtons = new JPanel(new GridLayout(2, 1));
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		panelFileSelection.add(panelButtons, gbc);

		JButton buttonAdd = new JButton(new ImageIcon(Viewer.class.getResource("/images/add.gif"), "add picture"));
		buttonAdd.addActionListener(new ListenerAddPicture());
		panelButtons.add(buttonAdd);

		JButton buttonDelete = new JButton(new ImageIcon(Viewer.class.getResource("/images/remove.gif"), "delete picture"));
		buttonDelete.addActionListener(new ListenerRemovePicture());
		panelButtons.add(buttonDelete);

		SpringUtilities.makeCompactGrid(this, 5, 2, 6, 6, 6, 6);
	}

	public EventDialog(JComboBox<String> instruments, Event event) {
		this(instruments);

		// copy information of given event
		comboBoxInstrument.setSelectedItem(event.getInstrument().getName());
		comboBoxInstrument.setEnabled(false);
		model.setValue(new Date(event.getDate().getTime()));
		model.setSelected(true);
		datePicker.remove(1);	// ugly hack to disable the button
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

	private ImageIcon byteArrayToScaledIcon(byte[] arr) {
		try {
			Image img = ImageIO.read(new ByteArrayInputStream(arr));
			Dimension scaledDimension = getScaledDimension(img.getWidth(null), img.getHeight(null), MAX_WIDTH, MAX_HEIGHT);
			img = img.getScaledInstance((int)scaledDimension.getWidth(), (int)scaledDimension.getHeight(), Image.SCALE_SMOOTH);

			return new ImageIcon(img);
		} catch(IOException e1) {
			JOptionPane.showMessageDialog(null, "Error while reading the selected picture file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	private Dimension getScaledDimension(int original_width, int original_height, int bound_width, int bound_height) {

		int new_width = original_width;
		int new_height = original_height;

		// first check if we need to scale width
		if (original_width > bound_width) {
			//scale width to fit
			new_width = bound_width;
			//scale height to maintain aspect ratio
			new_height = (new_width * original_height) / original_width;
		}

		// then check if we need to scale even with the new height
		if (new_height > bound_height) {
			//scale height to fit instead
			new_height = bound_height;
			//scale width to maintain aspect ratio
			new_width = (new_height * original_width) / original_height;
		}

		return new Dimension(new_width, new_height);
	}

	private class ListenerAddPicture implements ActionListener {

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

	private class ListenerRemovePicture implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			pictureField.setIcon(null);
			picture = null;
		}
	}
}
