package inspector.jmondb.viewer;

import inspector.jmondb.model.EventType;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultStyledDocument;
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
import org.apache.commons.io.IOUtils;

public class EventDialog extends JPanel {

	// instrument name
	private JComboBox<String> comboBoxInstrument;
	// event date
	private UtilDateModel model;
	private JDatePickerDisableImpl datePicker;
	// event type
	private JComboBox<EventType> comboBoxType;
	// description text
	private JTextArea textDescription;
	// description picture
	private JLabel thumbnailField;
	private byte[] picture;
	private static int MAX_THUMBNAIL_HEIGHT = 100;
	private static int MAX_THUMBNAIL_WIDTH = 150;
	private Icon iconThumbnail;
	private Icon iconFullSize;

	public EventDialog(JComboBox<String> instruments) {
		setLayout(new SpringLayout());

		// instrument name
		JLabel labelInstrument = new JLabel("Instrument name: ", JLabel.TRAILING);
		add(labelInstrument);
		comboBoxInstrument = new JComboBox<>();
		// add all instrument names
		// this has to be a different combobox because we don't want our choices here to influence the general application
		for(int i = 0; i < instruments.getItemCount(); i++)
			comboBoxInstrument.addItem(instruments.getItemAt(i));
		// select the previously selected instrument
		comboBoxInstrument.setSelectedIndex(instruments.getSelectedIndex());
		add(comboBoxInstrument);

		// date
		JLabel labelDate = new JLabel("Date: ", JLabel.TRAILING);
		add(labelDate);
		model = new UtilDateModel();
		JDatePanelImpl datePanel = new JDatePanelImpl(model);
		//TODO: override datePicker so the button can be disabled
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
		add(datePicker);

		// event type
		JLabel labelType = new JLabel("Event type: ", JLabel.TRAILING);
		add(labelType);
		comboBoxType = new JComboBox<>();
		for(EventType type : EventType.values())
			comboBoxType.addItem(type);
		add(comboBoxType);

		// description
		JLabel labelDescription = new JLabel("Description: ", JLabel.TRAILING);
		add(labelDescription);
		textDescription = new JTextArea();
		textDescription.setLineWrap(true);
		textDescription.setWrapStyleWord(true);
		DefaultStyledDocument doc = new DefaultStyledDocument();
		doc.setDocumentFilter(new DocumentSizeFilter(255));
		textDescription.setDocument(doc);

		JScrollPane descriptionScrollPane = new JScrollPane(textDescription);
		descriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		descriptionScrollPane.setPreferredSize(new Dimension(100, 100));
		add(descriptionScrollPane);

		// picture attachment
		JLabel labelPicture = new JLabel("Picture: ", JLabel.TRAILING);
		add(labelPicture);
		JPanel panelFileSelection = new JPanel(new GridBagLayout());
		add(panelFileSelection);
		GridBagConstraints gbc = new GridBagConstraints();

		thumbnailField = new JLabel();
		thumbnailField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2 && picture != null) {
					// show larger image in a pop-up
					JLabel labelPicture = new JLabel();
					labelPicture.setHorizontalAlignment(SwingConstants.CENTER);
					labelPicture.setVerticalAlignment(SwingConstants.CENTER);
					labelPicture.setIcon(iconFullSize);

					JOptionPane.showMessageDialog(null, labelPicture, "Event picture", JOptionPane.PLAIN_MESSAGE, null);
				}
			}
		});
		thumbnailField.setHorizontalAlignment(SwingConstants.CENTER);
		thumbnailField.setVerticalAlignment(SwingConstants.CENTER);
		thumbnailField.setPreferredSize(new Dimension(MAX_THUMBNAIL_WIDTH, MAX_THUMBNAIL_HEIGHT));
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		panelFileSelection.add(thumbnailField, gbc);

		JPanel panelButtons = new JPanel(new GridLayout(2, 1));
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		panelFileSelection.add(panelButtons, gbc);

		JButton buttonAdd = new JButton(new ImageIcon(getClass().getResource("/images/folder.png"), "add picture"));
		buttonAdd.addActionListener(new ListenerAddPicture());
		panelButtons.add(buttonAdd);

		JButton buttonDelete = new JButton(new ImageIcon(getClass().getResource("/images/garbage.png"), "delete picture"));
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
		datePicker.setButtonEnabled(false);
		comboBoxType.setSelectedItem(event.getType());
		comboBoxType.setEnabled(false);
		textDescription.setText(event.getDescription());
		if(event.getPicture() != null) {
			picture = event.getPicture();
			setIcons();
		}
	}

	private void setIcons() {
		Thread pictureGenerator = new Thread() {
			public void run() {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				iconThumbnail = byteArrayToScaledIcon(picture, MAX_THUMBNAIL_WIDTH, MAX_THUMBNAIL_HEIGHT);
				Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
				iconFullSize = byteArrayToScaledIcon(picture, screenSize.width, screenSize.height-200);

				thumbnailField.setIcon(iconThumbnail);

				setCursor(Cursor.getDefaultCursor());
			}
		};
		pictureGenerator.start();
	}

	private ImageIcon byteArrayToScaledIcon(byte[] array, int maxWidth, int maxHeight) {
		try {
			Image img = ImageIO.read(new ByteArrayInputStream(array));
			Dimension scaledDimension = getScaledDimension(img.getWidth(null), img.getHeight(null), maxWidth, maxHeight);
			img = img.getScaledInstance((int) scaledDimension.getWidth(), (int) scaledDimension.getHeight(), Image.SCALE_SMOOTH);

			return new ImageIcon(img);
		} catch(IOException e1) {
			JOptionPane.showMessageDialog(this, "Error while reading the selected picture file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	private Dimension getScaledDimension(int original_width, int original_height, int bound_width, int bound_height) {
		int new_width = original_width;
		int new_height = original_height;
		// first check if we need to scale width
		if(original_width > bound_width) {
			// scale width to fit
			new_width = bound_width;
			// scale height to maintain aspect ratio
			new_height = (new_width * original_height) / original_width;
		}
		// then check if we need to scale even with the new height
		if(new_height > bound_height) {
			// scale height to fit instead
			new_height = bound_height;
			// scale width to maintain aspect ratio
			new_width = (new_height * original_width) / original_height;
		}

		return new Dimension(new_width, new_height);
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
		String text = textDescription.getText();
		if(!text.equals(""))
			return text;
		else
			return null;
	}

	public byte[] getPicture() {
		return picture;
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
					setIcons();
				} catch(IOException e1) {
					JOptionPane.showMessageDialog(null, "Selected picture file not found", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private class ListenerRemovePicture implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			thumbnailField.setIcon(null);
			picture = null;
			iconThumbnail = null;
			iconFullSize = null;
		}
	}
}
