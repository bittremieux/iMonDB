package inspector.jmondb.model;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class RunTest {

	private Run run;

	@Before
	public void setUp() {
		run = new Run("run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()));

		for(int i = 0; i < 12; i++) {
			run.addValue(new ValueBuilder().setName("value_" + i).setAccession("value_" + i).setType("test").isNumeric(true).setFirstValue(Double.toString(Math.random() * 100)).createValue());
		}
	}

	@Test
	public void getValue_null() {
		assertNull(run.getValue(null));
	}

	@Test
	public void getValue_nonExisting() {
		assertNull(run.getValue("non-existing"));
	}

	@Test
	public void getValue_existing() {
		assertNotNull(run.getValue("value_5"));
	}

	@Test(expected=NullPointerException.class)
	public void addValue_null() {
		run.addValue(null);
	}

	@Test
	public void addValue_duplicate() {
		int nrOfValues = run.getNumberOfValues();
		run.addValue(new ValueBuilder().setName("value_7").setAccession("value_7").setType("test").isNumeric(true).setFirstValue("new value").createValue());
		assertEquals(nrOfValues, run.getNumberOfValues());
		Value v = run.getValue("value_7");
		assertEquals("new value", run.getValue("value_7").getFirstValue());
	}

	@Test
	public void addValue_new() {
		int nrOfValues = run.getNumberOfValues();
		run.addValue(new ValueBuilder().setName("new value").setAccession("new value").setType("test").isNumeric(true).setFirstValue(Double.toString(Math.random() * 100)).createValue());
		assertEquals(nrOfValues + 1, run.getNumberOfValues());
	}

	@Test
	public void removeValue_null() {
		int nrOfValues = run.getNumberOfValues();
		run.removeValue(null);
		assertEquals(nrOfValues, run.getNumberOfValues());
	}

	@Test
	public void removeValue_nonExisting() {
		int nrOfValues = run.getNumberOfValues();
		run.removeValue("non-existing value");
		assertEquals(nrOfValues, run.getNumberOfValues());
	}

	@Test
	public void removeValue_valid() {
		int nrOfValues = run.getNumberOfValues();
		run.removeValue("value_3");
		assertEquals(nrOfValues - 1, run.getNumberOfValues());
		assertNull(run.getValue("value_3"));
	}

}
