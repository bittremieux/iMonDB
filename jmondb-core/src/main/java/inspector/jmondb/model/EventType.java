package inspector.jmondb.model;

public enum EventType {

	CALIBRATION("calibration"),
	MAINTENANCE("maintenance"),
	INCIDENT("incident");

	private final String type;

	EventType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}
