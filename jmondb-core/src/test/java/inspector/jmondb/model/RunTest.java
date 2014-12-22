package inspector.jmondb.model;

/*
 * #%L
 * jMonDB Core
 * %%
 * Copyright (C) 2014 InSPECtor
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import static org.junit.Assert.*;

public class RunTest {

	private Run run;

	private final CV cv = new CV("testCv", "Dummy CV to run the unit tests", "https://bitbucket.org/proteinspector/jmondb/", "1");
	private final Instrument instrument = new Instrument("name", InstrumentModel.UNKNOWN_MODEL, cv);

	private ArrayList<Property> properties;

	final int NR_OF_METADATA = 7;

	@Before
	public void setUp() {
		final int NR_OF_VALUES = 12;

		properties = new ArrayList<>(NR_OF_VALUES);
		run = new Run("run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()), instrument);

		for(int i = 0; i < NR_OF_VALUES; i++) {
			Property prop = new Property("property_" + i, "test", "accession_" + i, cv, true);
			properties.add(prop);
			new ValueBuilder().setFirstValue(Double.toString(Math.random() * 1000)).setDefiningProperty(prop).setOriginatingRun(run).createValue();
		}

		for(int i = 0; i < NR_OF_METADATA; i++)
			new Metadata("meta" + i, "value" + i, run);
	}

	@Test(expected = NullPointerException.class)
	public void setName_null() {
		new Run(null, "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()), instrument);
	}

	@Test(expected = NullPointerException.class)
	public void setStorageName_null() {
		new Run("run", null, new Timestamp(Calendar.getInstance().getTime().getTime()), instrument);
	}

	@Test(expected = NullPointerException.class)
	public void setSampleDate_null() {
		new Run("run", "path/to/run/", null, instrument);
	}

	@Test(expected = NullPointerException.class)
	public void setInstrument_null() {
		new Run("run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()), null);
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
		assertNotEquals(oldValue, property.getValue(run));
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

	@Test
	public void equals() {
		Run runName = new Run("other name", "path/to/run/", run.getSampleDate(), instrument);
		Run runStorage = new Run("run", "other path", run.getSampleDate(), instrument);
		Run runDate = new Run("run", "path/to/run/", new Timestamp(run.getSampleDate().getTime() - 10000), instrument);
		Run runInstrument = new Run("run", "path/to/run/", run.getSampleDate(), new Instrument("other instrument", InstrumentModel.UNKNOWN_MODEL, cv));
		Run runIdentical = new Run("run", "path/to/run/", run.getSampleDate(), instrument);

		assertEquals(run, run);
		assertNotEquals(run, null);
		assertNotEquals(run, new Object());
		assertNotEquals(run, runName);
		assertNotEquals(run, runStorage);
		assertNotEquals(run, runDate);
		assertNotEquals(run, runInstrument);
		assertEquals(run, runIdentical);
	}
}
