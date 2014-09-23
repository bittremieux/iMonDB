package inspector.jmondb.intervention;

import java.util.Date;

public class Intervention implements Comparable<Intervention> {

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

	@Override
	public int compareTo(Intervention o) {
		if(getDate().before(o.getDate()))
			return -1;
		else if(getDate().after(o.getDate()))
			return 1;
		else {
			int score = 0;
			if(isCalibrationCheck())
				score += 1;
			if(isCalibration())
				score += 2;
			if(isEvent())
				score += 4;
			if(isIncident())
				score += 8;
			int oScore = 0;
			if(o.isCalibrationCheck())
				oScore += 1;
			if(o.isCalibration())
				oScore += 2;
			if(o.isEvent())
				oScore += 4;
			if(o.isIncident())
				oScore += 8;

			if(score < oScore)
				return -1;
			else if(score > oScore)
				return 1;
			else
				return 0;
		}
	}
}
