package inspector.jmondb.io;

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

import inspector.jmondb.model.*;
import org.hibernate.LazyInitializationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class IMonDBWriterReaderTest {

	private EntityManagerFactory emf;

	private ArrayList<Instrument> instruments;

	@Before
	public void setUp() {
		emf = IMonDBManagerFactory.createMySQLFactory(null, null, "iMonDBtest", "iMonDB", "iMonDB");

		instruments = new ArrayList<>();

		// create fully populated objects
		CV cvInstr = new CV("MS", "PSI MS controlled vocabulary", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0");
		CV cvImon = new CV("IMon", "IMonDB to be created controlled vocabulary", "https://bitbucket.org/proteinspector/jmondb/", "0.0.1");

		ArrayList<Property> properties = new ArrayList<>(1009);
		for(int p = 0; p < 1009; p++) {
			Property prop = new Property("property_" + p, "test", "IMon:" + p, cvImon, true);
			properties.add(prop);
		}

		for(int i = 0; i < 3; i++) {
			Instrument instrument = new Instrument("instrument_" + i, InstrumentModel.THERMO_Q_EXACTIVE, cvInstr);
			instruments.add(instrument);

			Timestamp lastTime = new Timestamp(1264978800000L);
			for(int r = 0; r < 31; r++) {
				Run run = new Run("run_" + r, "path/to/run/" + r, lastTime, instrument);
				lastTime = new Timestamp(lastTime.getTime() + 86400000L * (r+1));

				for(int v = 0; v < 251; v++) {
					new Value(Double.toString(Math.random() * 10000), (int) (Math.random() * 10000), (int) (Math.random() * 10000),
							Math.random() * 10000, Math.random() * 10000, Math.random() * 10000, Math.random() * 10000, Math.random() * 10000,
							Math.random() * 10000, Math.random() * 10000, properties.get((int) (Math.random() * properties.size())), run);
				}

				for(int m = 0; m < 7; m++)
					new Metadata("meta_" + m, "value_" + m, run);
			}

			lastTime = new Timestamp(1264978800000L);
			for(int e = 0; e < 13; e++) {
				EventType type = Math.random() < 0.3 ? EventType.CALIBRATION : Math.random() > 0.6 ? EventType.MAINTENANCE : EventType.INCIDENT;
				new Event(instrument, lastTime, type, "problem " + e, "solution " + e, "extra " + e);
				lastTime = new Timestamp(lastTime.getTime() + 172800000L * (e+1));
			}
		}
	}

	@After
	public void tearDown() {
		EntityManager em = emf.createEntityManager();

		// clear all existing data
		em.getTransaction().begin();
		em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_value").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_property").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_metadata").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_run").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_event").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_instrument_properties").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_instrument").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_cv").executeUpdate();
		em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
		em.getTransaction().commit();

		emf.close();
	}

	@Test(expected = NullPointerException.class)
	public void writeInstrument_null() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(null);
	}

	@Test
	public void writeInstrument_new() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		instruments.forEach(writer::writeInstrument);

		IMonDBReader reader = new IMonDBReader(emf);
		for(Instrument inst : instruments) {
			Instrument instRead = reader.getInstrument(inst.getName(), false, true);
			assertNotNull(instRead);
			// verify that the properties are the same
			for(Iterator<Property> it = instRead.getPropertyIterator(); it.hasNext(); ) {
				Property prop = it.next();
				assertEquals(inst.getProperty(prop.getAccession()), prop);
			}
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void writeInstrument_duplicate() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		instruments.forEach(writer::writeInstrument);

		writer.writeInstrument(instruments.get(0));
	}

	@Test(expected = NullPointerException.class)
	public void writeEvent_null() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeOrUpdateEvent(null);
	}

	@Test(expected = IllegalStateException.class)
	public void writeEvent_noInstrument() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeOrUpdateEvent(instruments.get(0).getEventIterator().next());
	}

	@Test(expected = LazyInitializationException.class)
	public void writeEvent_newLazyException() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));

		for(Iterator<Event> eventIt = instruments.get(0).getEventIterator(); eventIt.hasNext(); )
			writer.writeOrUpdateEvent(eventIt.next());

		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(instruments.get(0).getName(), false, false).getEventIterator().hasNext();
	}

	@Test
	public void writeEvent_new() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));

		for(Iterator<Event> eventIt = instruments.get(0).getEventIterator(); eventIt.hasNext(); )
			writer.writeOrUpdateEvent(eventIt.next());

		IMonDBReader reader = new IMonDBReader(emf);
		Instrument instr = reader.getInstrument(instruments.get(0).getName(), true, false);
		for(Iterator<Event> eventIt = instruments.get(0).getEventIterator(); eventIt.hasNext(); ) {
			Event event = eventIt.next();
			assertEquals(event, instr.getEvent(event.getDate()));
		}
	}

	@Test
	public void writeEvent_duplicate() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));

		for(Iterator<Event> eventIt = instruments.get(0).getEventIterator(); eventIt.hasNext(); )
			writer.writeOrUpdateEvent(eventIt.next());

		Event eventOld = instruments.get(0).getEventIterator().next();
		Event eventNew = new Event(instruments.get(0), eventOld.getDate(), EventType.INCIDENT, "a new problem", "a new solution", "some extra text");
		writer.writeOrUpdateEvent(eventNew);

		IMonDBReader reader = new IMonDBReader(emf);
		Instrument instr = reader.getInstrument(instruments.get(0).getName(), true, false);
		assertNotEquals(eventOld.getProblem(), instr.getEvent(eventOld.getDate()).getProblem());
		assertEquals(eventNew.getProblem(), instr.getEvent(eventOld.getDate()).getProblem());
		assertNotEquals(eventOld.getSolution(), instr.getEvent(eventOld.getDate()).getSolution());
		assertEquals(eventNew.getSolution(), instr.getEvent(eventOld.getDate()).getSolution());
		assertNotEquals(eventOld.getExtra(), instr.getEvent(eventOld.getDate()).getExtra());
		assertEquals(eventNew.getExtra(), instr.getEvent(eventOld.getDate()).getExtra());
	}

	@Test(expected = NullPointerException.class)
	public void writeRun_null() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeRun(null);
	}

	@Test(expected = IllegalStateException.class)
	public void writeRun_noInstrument() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeRun(instruments.get(0).getRunIterator().next());
	}

	@Test
	public void writeRun_new() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		Run runOld = instruments.get(0).getRunIterator().next();
		writer.writeRun(runOld);

		IMonDBReader reader = new IMonDBReader(emf);
		Run runNew = reader.getRun(runOld.getName(), runOld.getInstrument().getName());
		assertEquals(runOld, runNew);
		// all metadata
		for(Iterator<Metadata> metaIt = runNew.getMetadataIterator(); metaIt.hasNext(); ) {
			Metadata md = metaIt.next();
			assertEquals(runOld.getMetadata(md.getName()), md);
		}
		// all child values
		for(Iterator<Value> valIt = runNew.getValueIterator(); valIt.hasNext(); ) {
			Value value = valIt.next();
			assertEquals(value, runOld.getValue(value.getDefiningProperty()));
		}
		// check map keys in both directions
		for(Iterator<Value> valIt = runOld.getValueIterator(); valIt.hasNext(); ) {
			Value value = valIt.next();
			assertEquals(value, runNew.getValue(value.getDefiningProperty()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void writeRun_duplicate() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());
		writer.writeRun(instruments.get(0).getRunIterator().next());
	}

	@Test(expected = NullPointerException.class)
	public void writeProperty_null() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeProperty(null);
	}

	@Test
	public void writeProperty_newNoValues() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Property propOld = instruments.get(0).getRunIterator().next().getValueIterator().next().getDefiningProperty();
		writer.writeProperty(propOld);

		IMonDBReader reader = new IMonDBReader(emf);
		Property propNew = reader.getProperty(propOld.getAccession());
		assertEquals(propOld, propNew);
		assertFalse(propNew.getValueIterator().hasNext());
	}

	@Test
	public void writeProperty_newHasValues() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		for(Instrument inst : instruments) {
			writer.writeInstrument(inst);
			for(Iterator<Run> runIt = inst.getRunIterator(); runIt.hasNext(); )
				writer.writeRun(runIt.next());
		}

		IMonDBReader reader = new IMonDBReader(emf);
		Property propOld = instruments.get(0).getRunIterator().next().getValueIterator().next().getDefiningProperty();
		Property propNew = reader.getProperty(propOld.getAccession());
		assertEquals(propOld, propNew);

		for(Iterator<Value> valIt = propNew.getValueIterator(); valIt.hasNext(); ) {
			Value value = valIt.next();
			assertNotNull(propOld.getValue(value.getOriginatingRun()));
			assertEquals(value, propOld.getValue(value.getOriginatingRun()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void writeProperty_duplicate() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Property prop = instruments.get(0).getRunIterator().next().getValueIterator().next().getDefiningProperty();
		writer.writeProperty(prop);
		writer.writeProperty(prop);
	}

	@Test(expected = NullPointerException.class)
	public void writeCv_null() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeCv(null);
	}

	@Test
	public void writeCv_new() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeCv(instruments.get(0).getCv());
	}

	@Test
	public void writeCv_duplicate() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeCv(instruments.get(0).getCv());
		writer.writeCv(instruments.get(0).getCv());
	}

	@Test
	public void getInstrument_null() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		assertNull(reader.getInstrument(null));
	}

	@Test
	public void getInstrument_nonExisting() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		assertNull(reader.getInstrument("non-existing instrument"));
	}

	@Test
	public void getRun_nullRun() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		assertNull(reader.getRun(null, instruments.get(0).getName()));
	}

	@Test
	public void getRun_nullInstrument() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		assertNull(reader.getRun(instruments.get(0).getRunIterator().next().getName(), null));
	}

	@Test
	public void getRun_nonExistingRun() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		assertNull(reader.getRun("non-existing run", instruments.get(0).getName()));
	}

	@Test
	public void getRun_nonExistingInstrument() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		assertNull(reader.getRun(instruments.get(0).getRunIterator().next().getName(), "non-existing instrument"));
	}

	@Test
	public void getProperty_null() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		assertNull(reader.getProperty(null));
	}

	@Test
	public void getProperty_nonExisting() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		assertNull(reader.getProperty("non-existing property"));
	}

	@Test
	public void getFromCustomQuery_nullQuery() {
		IMonDBReader reader = new IMonDBReader(emf);
		assertNull(reader.getFromCustomQuery(null, Instrument.class));
	}

	@Test
	public void getFromCustomQuery_nullClass() {
		IMonDBReader reader = new IMonDBReader(emf);
		assertNull(reader.getFromCustomQuery("SELECT inst FROM Instrument inst", null));
	}
}
