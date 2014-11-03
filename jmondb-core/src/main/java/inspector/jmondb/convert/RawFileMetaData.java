package inspector.jmondb.convert;

import inspector.jmondb.model.InstrumentModel;

import java.sql.Timestamp;

/**
 * Helper class to group raw file meta data, such as the sample date and the instrument model.
 */
public class RawFileMetadata {

	private final Timestamp date;
	private final InstrumentModel model;

	public RawFileMetadata(Timestamp date, InstrumentModel model) {
		this.date = date;
		this.model = model;
	}

	public Timestamp getDate() {
		return date;
	}

	public InstrumentModel getModel() {
		return model;
	}
}
