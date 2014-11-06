package inspector.jmondb.model;

public enum EventType {

	UNDEFINED("undefined"),
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

	public static EventType fromString(String text) {
		if(text != null)
			for(EventType event : values())
				if(text.equals(event.toString()))
					return event;

		return null;
	}
}
