package inspector.jmondb.intervention;

import java.util.Date;

public class Intervention {

	private Date date;
	private boolean isCalibrationCheck;
	private boolean isCalibration;
	private boolean isEvent;
	private boolean isIncident;
	private String comment;

	public Intervention(Date date, boolean isCalibrationCheck, boolean isCalibration, boolean isEvent, boolean isIncident) {
		this.date = date;
		this.isCalibrationCheck = isCalibrationCheck;
		this.isCalibration = isCalibration;
		this.isEvent = isEvent;
		this.isIncident = isIncident;
	}
	public Intervention(Date date, boolean isCalibrationCheck, boolean isCalibration, boolean isEvent, boolean isIncident, String comment) {
		this(date, isCalibrationCheck, isCalibration, isEvent, isIncident);
		this.comment = comment;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isCalibrationCheck() {
		return isCalibrationCheck;
	}

	public void setCalibrationCheck(boolean isCalibrationCheck) {
		this.isCalibrationCheck = isCalibrationCheck;
	}

	public boolean isCalibration() {
		return isCalibration;
	}

	public void setCalibration(boolean isCalibration) {
		this.isCalibration = isCalibration;
	}

	public boolean isEvent() {
		return isEvent;
	}

	public void setEvent(boolean isEvent) {
		this.isEvent = isEvent;
	}

	public boolean isIncident() {
		return isIncident;
	}

	public void setIncident(boolean isIncident) {
		this.isIncident = isIncident;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
