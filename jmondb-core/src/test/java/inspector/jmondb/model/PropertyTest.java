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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PropertyTest {

	private Property property;

	private final CV cv = new CV("testCv", "Dummy CV to run the unit tests", "https://bitbucket.org/proteinspector/jmondb/", "1");
	private final Instrument instrument = new Instrument("name", InstrumentModel.UNKNOWN_MODEL, cv);

	private ArrayList<Run> runs;

	@Before
	public void setUp() {
		final int NR_OF_VALUES = 7;

		runs = new ArrayList<>(NR_OF_VALUES);
		property = new Property("property", "test", "accession", cv, true);

		for(int i = 0; i < NR_OF_VALUES; i++) {
			Run run = new Run("run_" + i, "path/to/run/" + i, new Timestamp(Calendar.getInstance().getTime().getTime()), instrument);
			runs.add(run);
			new ValueBuilder().setFirstValue(Double.toString(Math.random() * 1000)).setDefiningProperty(property).setOriginatingRun(run).createValue();
		}
	}

	@Test
	public void getValue_null() {
		assertNull(property.getValue(null));
	}

	@Test
	public void getValue_nonExisting() {
		Run run = new Run("run_new", "path/to/run/new/", new Timestamp(Calendar.getInstance().getTime().getTime()), instrument);
		assertNull(property.getValue(run));
	}

	@Test
	public void getValue_valid() {
		for(Run run : runs)
			assertNotNull(property.getValue(run));
	}

	@Test(expected=NullPointerException.class)
	public void assignValue_null() {
		property.assignValue(null);
	}

	@Test
	public void assignValue_duplicate() {
		Run run = runs.get((int)(Math.random() * runs.size()));
		Value oldValue = property.getValue(run);
		assertNotNull(oldValue);

		Value newValue = new ValueBuilder().setFirstValue(Double.toString(Math.random()*1000)).setDefiningProperty(property).setOriginatingRun(run).createValue();

		assertNotEquals(oldValue, property.getValue(run));
	}

	@Test
	public void assignValue_new() {
		Run run = new Run("run_new", "path/to/run/new/", new Timestamp(Calendar.getInstance().getTime().getTime()), instrument);
		assertNull(property.getValue(run));

		new ValueBuilder().setFirstValue(Double.toString(Math.random()*1000)).setDefiningProperty(property).setOriginatingRun(run).createValue();

		assertNotNull(property.getValue(run));
	}
}
