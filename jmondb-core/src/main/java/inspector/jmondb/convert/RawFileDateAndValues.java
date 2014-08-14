package inspector.jmondb.convert;

import inspector.jmondb.model.Value;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Helper class to combine both a {@link Timestamp} sample date and instrument {@link Value}s for a raw file.
 */
public class RawFileDateAndValues {

	private final Timestamp date;
	private final ArrayList<Value> values;

	public RawFileDateAndValues(Timestamp date, ArrayList<Value> values) {
		this.date = date;
		this.values = values;
	}

	public Timestamp getDate() {
		return date;
	}

	public ArrayList<Value> getValues() {
		return values;
	}
}
