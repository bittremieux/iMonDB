package inspector.jmondb.viewer;

import inspector.jmondb.model.Event;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InterventionDialog extends JPanel {

	private JDatePickerImpl datePicker;
	private UtilDateModel model;
	private JCheckBox checkBoxCalibrationCheck;
	private JCheckBox checkBoxCalibration;
	private JCheckBox checkBoxEvent;
	private JCheckBox checkBoxIncident;
	private JTextField textFieldComment;

	public InterventionDialog() {
		setLayout(new BorderLayout());

		// date
		JPanel panelDate = new JPanel(new GridLayout(0, 2));
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
		add(panelDate, BorderLayout.PAGE_START);

		// intervention type
		JPanel panelType = new JPanel(new GridLayout(3, 2));
		JLabel labelType = new JLabel("Intervention type:");
		panelType.add(labelType);
		panelType.add(new JLabel(""));
		checkBoxCalibrationCheck = new JCheckBox("Calibration check");
		panelType.add(checkBoxCalibrationCheck);
		checkBoxCalibration = new JCheckBox("Calibration");
		panelType.add(checkBoxCalibration);
		checkBoxEvent = new JCheckBox("Event");
		panelType.add(checkBoxEvent);
		checkBoxIncident = new JCheckBox("Incident");
		panelType.add(checkBoxIncident);
		add(panelType, BorderLayout.CENTER);

		// comment
		JPanel panelComment = new JPanel(new GridLayout(0, 2));
		JLabel labelComment = new JLabel("Comment: ");
		panelComment.add(labelComment);
		textFieldComment = new JTextField();
		panelComment.add(textFieldComment);
		add(panelComment, BorderLayout.PAGE_END);
	}

	public InterventionDialog(Event intervention) {
		this();

		model.setValue(intervention.getDate());
		//checkBoxCalibrationCheck.setSelected(intervention.isCalibrationCheck());
		//checkBoxCalibration.setSelected(intervention.isCalibration());
		//checkBoxEvent.setSelected(intervention.isEvent());
		//checkBoxIncident.setSelected(intervention.isIncident());
		textFieldComment.setText(intervention.getDescription());
	}

	public InterventionDialog(inspector.jmondb.model.Event intervention, boolean showType) {
		this(intervention);

		checkBoxCalibrationCheck.setEnabled(false);
		checkBoxCalibration.setEnabled(false);
		checkBoxEvent.setEnabled(false);
		checkBoxIncident.setEnabled(false);
	}

	public Date getDate() {
		return (Date) datePicker.getModel().getValue();
	}

	public boolean isCalibrationCheck() {
		return checkBoxCalibrationCheck.isSelected();
	}

	public boolean isCalibration() {
		return checkBoxCalibration.isSelected();
	}

	public boolean isEvent() {
		return checkBoxEvent.isSelected();
	}

	public boolean isIncident() {
		return checkBoxIncident.isSelected();
	}

	public String getComment() {
		return textFieldComment.getText();
	}

}
