package inspector.jmondb.model;

public enum InstrumentModel {

	/* Thermo instruments */
	THERMO_LTQ_ORBITRAP("MS:1000449"),
	THERMO_ORBITRAP_XL("MS:1000556"),
	THERMO_LTQ_VELOS("MS:1000855"),
	THERMO_TSQ_VANTAGE("MS:1001510"),
	THERMO_ORBITRAP_VELOS("MS:1001742"),
	THERMO_Q_EXACTIVE("MS:1001911"),
	THERMO_ORBITRAP_FUSION("MS:1002416"),

	/* general instruments */
	UNKNOWN_MODEL("MS:1000031");

	private final String cvAccession;

	InstrumentModel(String accession) {
		this.cvAccession = accession;
	}

	@Override
	public String toString() {
		return cvAccession;
	}

	public static InstrumentModel fromString(String text) {
		if(text != null)
			for(InstrumentModel model : values())
				if(text.equals(model.toString()))
					return model;

		return UNKNOWN_MODEL;
	}
}
