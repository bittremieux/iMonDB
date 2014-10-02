package inspector.jmondb.model;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class RunTest {

	private Run run;

	private final CV cv = new CV("testCv", "Dummy CV to run the unit tests", "https://bitbucket.org/proteinspector/jmondb/", "1");

	private ArrayList<Property> properties;

	final int NR_OF_METADATA = 7;

	@Before
	public void setUp() {
		final int NR_OF_VALUES = 12;

		properties = new ArrayList<>(NR_OF_VALUES);
		Instrument instrument = new Instrument("name", InstrumentModel.UNKNOWN_MODEL, cv);
		run = new Run("run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()), instrument);

		for(int i = 0; i < NR_OF_VALUES; i++) {
			Property prop = new Property("property_" + i, "test", "accession_" + i, cv, true);
			properties.add(prop);
			new ValueBuilder().setFirstValue(Double.toString(Math.random() * 1000)).setDefiningProperty(prop).setOriginatingRun(run).createValue();
		}

		for(int i = 0; i < NR_OF_METADATA; i++)
			new Metadata("meta" + i, "value" + i, run);
	}

	@Test
	public void getValue_null() {
		assertNull(run.getValue(null));
	}

	@Test
	public void getValue_nonExisting() {
		Property prop = new Property("property_new", "test", "accession_new", cv, true);
		assertNull(run.getValue(prop));
	}

	@Test
	public void getValue_valid() {
		for(Property prop : properties)
			assertNotNull(run.getValue(prop));
	}

	@Test(expected=NullPointerException.class)
	public void addValue_null() {
		run.addValue(null);
	}

	@Test
	public void addValue_duplicate() {
		Property property = properties.get((int)(Math.random() * properties.size()));
		Value oldValue = run.getValue(property);
		assertNotNull(oldValue);

		Value newValue = new ValueBuilder().setFirstValue(Double.toString(Math.random()*1000)).setDefiningProperty(property).setOriginatingRun(run).createValue();

		assertNotEquals(oldValue, run.getValue(property));
	}

	@Test
	public void addValue_new() {
		Property property = new Property("property_new", "test", "accession_new", cv, true);
		assertNull(run.getValue(property));

		new ValueBuilder().setFirstValue(Double.toString(Math.random()*1000)).setDefiningProperty(property).setOriginatingRun(run).createValue();

		assertNotNull(run.getValue(property));
	}

	@Test
	public void getMetadata_null() {
		assertNull(run.getMetadata(null));
	}

	@Test
	public void getMetadata_nonExisting() {
		assertNull(run.getMetadata("non-existing metadata"));
	}

	@Test
	public void getMetadata_valid() {
		for(int i = 0; i < NR_OF_METADATA; i++)
			assertNotNull(run.getMetadata("meta" + i));
	}

	@Test(expected=NullPointerException.class)
	public void addMetadata_null() {
		run.addMetadata(null);
	}

	@Test
	public void addMetadata_duplicate() {
		Metadata oldMeta = run.getMetadata("meta" + (int)(Math.random() * NR_OF_METADATA));
		String name = oldMeta.getName();
		assertNotNull(oldMeta);

		new Metadata(name, "new value", run);

		assertNotEquals(oldMeta, run.getMetadata(name));
	}

	@Test
	public void addMetadata_new() {
		String name = "new metadata";

		assertNull(run.getMetadata(name));

		new Metadata(name, "new value", run);

		assertNotNull(run.getMetadata(name));
	}
}
