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

	@Before
	public void setUp() {
		final int NR_OF_VALUES = 12;

		properties = new ArrayList<>(NR_OF_VALUES);
		run = new Run("run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()));

		for(int i = 0; i < NR_OF_VALUES; i++) {
			Property prop = new Property("property_" + i, "test", "accession_" + i, cv, true);
			properties.add(prop);
			new ValueBuilder().setFirstValue(Double.toString(Math.random() * 1000)).setDefiningProperty(prop).setOriginatingRun(run).createValue();
		}
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
}
