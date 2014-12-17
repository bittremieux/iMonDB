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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.*;

public class IMonDBWriterReaderIT {

	private static final String PORT = System.getProperty("mysql.port");

	private EntityManagerFactory emf;

	private ArrayList<Instrument> instruments;

	private final CV cvImon = new CV("IMon", "IMonDB to be created controlled vocabulary", "https://bitbucket.org/proteinspector/jmondb/", "0.0.1");

	@Before
	public void setUp() {
		emf = IMonDBManagerFactory.createMySQLFactory("localhost", PORT, "root", "root", "root");

		instruments = new ArrayList<>();

		// create fully populated objects
		CV cvInstr = new CV("MS", "PSI MS controlled vocabulary", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0");

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
	public void iMonDBReader_null() {
		new IMonDBReader(null);
	}

	@Test(expected = NullPointerException.class)
	public void iMonDBWriter_null() {
		new IMonDBWriter(null);
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
			Instrument instRead = reader.getInstrument(inst.getName(), true, true);
			assertNotNull(instRead);
			assertFalse(instRead.getRunIterator().hasNext());
			// verify that the properties are the same
			for(Iterator<Property> it = instRead.getPropertyIterator(); it.hasNext(); ) {
				Property prop = it.next();
				assertEquals(inst.getProperty(prop.getAccession()), prop);
			}
			// verify that the events are the same
			for(Iterator<Event> it = instRead.getEventIterator(); it.hasNext(); ) {
				Event event = it.next();
				assertEquals(inst.getEvent(event.getDate()), event);
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

	@Test(expected = NullPointerException.class)
	public void removeEvent_nullInstrument() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		for(Iterator<Event> it = instruments.get(0).getEventIterator(); it.hasNext(); ) {
			writer.writeOrUpdateEvent(it.next());
		}
		Event event = instruments.get(0).getEventIterator().next();
		writer.removeEvent(null, event.getDate());
	}

	@Test(expected = NullPointerException.class)
	public void removeEvent_nullDate() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		for(Iterator<Event> it = instruments.get(0).getEventIterator(); it.hasNext(); ) {
			writer.writeOrUpdateEvent(it.next());
		}
		Event event = instruments.get(0).getEventIterator().next();
		writer.removeEvent(event.getInstrument().getName(), null);
	}

	@Test
	public void removeEvent_nonExistingInstrument() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		for(Iterator<Event> it = instruments.get(0).getEventIterator(); it.hasNext(); ) {
			writer.writeOrUpdateEvent(it.next());
		}
		IMonDBReader reader = new IMonDBReader(emf);
		Event event = instruments.get(0).getEventIterator().next();

		int nrOfEventsOld = 0;
		for(Iterator<Event> it = reader.getInstrument(event.getInstrument().getName(), true, false).getEventIterator(); it.hasNext(); ) {
			it.next();
			nrOfEventsOld++;
		}
		writer.removeEvent("non-existing instrument", event.getDate());
		int nrOfEventsNew = 0;
		for(Iterator<Event> it = reader.getInstrument(event.getInstrument().getName(), true, false).getEventIterator(); it.hasNext(); ) {
			it.next();
			nrOfEventsNew++;
		}
		assertEquals(nrOfEventsOld, nrOfEventsNew);
	}

	@Test
	public void removeEvent_nonExistingDate() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		for(Iterator<Event> it = instruments.get(0).getEventIterator(); it.hasNext(); ) {
			writer.writeOrUpdateEvent(it.next());
		}
		IMonDBReader reader = new IMonDBReader(emf);
		Event event = instruments.get(0).getEventIterator().next();

		int nrOfEventsOld = 0;
		for(Iterator<Event> it = reader.getInstrument(event.getInstrument().getName(), true, false).getEventIterator(); it.hasNext(); ) {
			it.next();
			nrOfEventsOld++;
		}
		writer.removeEvent(event.getInstrument().getName(), new Timestamp(0));
		int nrOfEventsNew = 0;
		for(Iterator<Event> it = reader.getInstrument(event.getInstrument().getName(), true, false).getEventIterator(); it.hasNext(); ) {
			it.next();
			nrOfEventsNew++;
		}
		assertEquals(nrOfEventsOld, nrOfEventsNew);
	}

	@Test
	public void removeEvent() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		for(Iterator<Event> it = instruments.get(0).getEventIterator(); it.hasNext(); ) {
				writer.writeOrUpdateEvent(it.next());
		}
		IMonDBReader reader = new IMonDBReader(emf);
		Event event = instruments.get(0).getEventIterator().next();

		int nrOfEventsOld = 0;
		for(Iterator<Event> it = reader.getInstrument(event.getInstrument().getName(), true, false).getEventIterator(); it.hasNext(); ) {
			it.next();
			nrOfEventsOld++;
		}
		writer.removeEvent(event.getInstrument().getName(), event.getDate());
		int nrOfEventsNew = 0;
		for(Iterator<Event> it = reader.getInstrument(event.getInstrument().getName(), true, false).getEventIterator(); it.hasNext(); ) {
			it.next();
			nrOfEventsNew++;
		}
		assertEquals(nrOfEventsOld - 1, nrOfEventsNew);
		assertNull(reader.getInstrument(event.getInstrument().getName(), true, false).getEvent(event.getDate()));
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
	public void getInstrument_noRun() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		Instrument instrument = reader.getInstrument(instruments.get(0).getName(), false, false);
		assertNotNull(instrument);
		assertFalse(instrument.getRunIterator().hasNext());
		assertNull(instrument.getRun(instruments.get(0).getRunIterator().next().getSampleDate()));
		Timestamp now = new Timestamp(new Date().getTime());
		assertTrue(instrument.getRunRange(new Timestamp(1264978800000L), now).isEmpty());

		Run run = new Run("run", "path", new Timestamp(now.getTime() - 10000), instrument);

		assertTrue(instrument.getRunIterator().hasNext());
		assertNotNull(instrument.getRun(run.getSampleDate()));
		assertFalse(instrument.getRunRange(new Timestamp(1264978800000L), now).isEmpty());
	}

	@Test
	public void getInstrument_excludeEvents() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());
		for(Iterator<Event> eventIt = instruments.get(0).getEventIterator(); eventIt.hasNext(); )
			writer.writeOrUpdateEvent(eventIt.next());

		IMonDBReader reader = new IMonDBReader(emf);
		Instrument instrument = reader.getInstrument(instruments.get(0).getName(), false, false);
		assertNotNull(instrument);
		assertFalse(instrument.getEventIterator().hasNext());
		assertNull(instrument.getEvent(instruments.get(0).getEventIterator().next().getDate()));
		Timestamp now = new Timestamp(new Date().getTime());
		assertTrue(instrument.getEventRange(new Timestamp(1264978800000L), now).isEmpty());

		Event event = new Event(instrument, new Timestamp(now.getTime() - 10000), EventType.UNDEFINED);

		assertTrue(instrument.getEventIterator().hasNext());
		assertNotNull(instrument.getEvent(event.getDate()));
		assertFalse(instrument.getEventRange(new Timestamp(1264978800000L), now).isEmpty());
	}

	@Test
	public void getInstrument_includeEvents() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());
		for(Iterator<Event> eventIt = instruments.get(0).getEventIterator(); eventIt.hasNext(); )
			writer.writeOrUpdateEvent(eventIt.next());

		IMonDBReader reader = new IMonDBReader(emf);
		Instrument instrument = reader.getInstrument(instruments.get(0).getName(), true, false);
		assertNotNull(instrument);
		assertTrue(instrument.getEventIterator().hasNext());
		for(Iterator<Event> eventIt = instruments.get(0).getEventIterator(); eventIt.hasNext(); )
			assertNotNull(instrument.getEvent(eventIt.next().getDate()));
	}

	@Test
	public void getInstrument_excludeProperties() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		Instrument instrument = reader.getInstrument(instruments.get(0).getName(), false, false);
		assertNotNull(instrument);
		assertFalse(instrument.getPropertyIterator().hasNext());
		assertNull(instrument.getProperty(instruments.get(0).getPropertyIterator().next().getAccession()));

		Property property = new Property("property", "test", "accession", cvImon, true);
		Run run = new Run("run", "path", new Timestamp(new Date().getTime()), instrument);
		new ValueBuilder().setFirstValue("value").setDefiningProperty(property).setOriginatingRun(run).createValue();

		assertTrue(instrument.getPropertyIterator().hasNext());
		assertNotNull(instrument.getProperty("accession"));
	}

	@Test
	public void getInstrument_includeProperties() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		Instrument instrument = reader.getInstrument(instruments.get(0).getName(), false, true);
		assertNotNull(instrument);
		assertTrue(instrument.getPropertyIterator().hasNext());
		for(Iterator<Property> it = instruments.get(0).getPropertyIterator(); it.hasNext(); )
			assertNotNull(instrument.getProperty(it.next().getAccession()));
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
	public void getRun() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		Run run = reader.getRun(instruments.get(0).getRunIterator().next().getName(), instruments.get(0).getName());

		assertNotNull(run);
		assertNotNull(run.getInstrument());
		assertEquals(run.getInstrument(), instruments.get(0));
		for(Iterator<Value> it = instruments.get(0).getRunIterator().next().getValueIterator(); it.hasNext(); ) {
			Property old = it.next().getDefiningProperty();

			Value value = run.getValue(old);
			assertNotNull(value);
			assertNotNull(value.getDefiningProperty());
			assertEquals(value.getDefiningProperty(), old);
			assertFalse(value.getDefiningProperty().getValueIterator().hasNext());
			assertEquals(run, value.getOriginatingRun());
		}
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
	public void getProperty() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));
		writer.writeRun(instruments.get(0).getRunIterator().next());

		IMonDBReader reader = new IMonDBReader(emf);
		for(Iterator<Value> it = instruments.get(0).getRunIterator().next().getValueIterator(); it.hasNext(); ) {
			Value oldValue = it.next();
			Property oldProp = oldValue.getDefiningProperty();

			Property property = reader.getProperty(oldProp.getAccession());
			Value value = property.getValue(oldValue.getOriginatingRun());

			assertNotNull(property);
			assertEquals(property, oldProp);
			assertEquals(property, value.getDefiningProperty());

			assertNotNull(value);
			assertNotNull(value.getOriginatingRun());
			assertEquals(value.getOriginatingRun(), oldValue.getOriginatingRun());
			assertNotNull(value.getOriginatingRun().getInstrument());
			assertEquals(value.getOriginatingRun().getInstrument(), oldValue.getOriginatingRun().getInstrument());
		}
	}

	@Test
	public void getFromCustomQuery_nullQuery() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));

		IMonDBReader reader = new IMonDBReader(emf);
		assertTrue(reader.getFromCustomQuery(null, Instrument.class).isEmpty());
	}

	@Test
	public void getFromCustomQuery_nullClass() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));

		IMonDBReader reader = new IMonDBReader(emf);
		assertTrue(reader.getFromCustomQuery("SELECT inst FROM Instrument inst", null).isEmpty());
	}

	@Test
	public void getFromCustomQuery_noParameters() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));

		IMonDBReader reader = new IMonDBReader(emf);
		List<Instrument> results = reader.getFromCustomQuery("SELECT inst FROM Instrument inst", Instrument.class);

		assertFalse(results.isEmpty());
	}

	@Test
	public void getFromCustomQuery_nullParameters() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));

		IMonDBReader reader = new IMonDBReader(emf);
		List<Instrument> results = reader.getFromCustomQuery("SELECT inst FROM Instrument inst", Instrument.class, null);

		assertFalse(results.isEmpty());
	}

	@Test
	public void getFromCustomQuery_emptyParameters() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));

		IMonDBReader reader = new IMonDBReader(emf);
		List<Instrument> results = reader.getFromCustomQuery("SELECT inst FROM Instrument inst", Instrument.class, new HashMap<>());

		assertFalse(results.isEmpty());
	}

	@Test
	public void getFromCustomQuery() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeInstrument(instruments.get(0));

		IMonDBReader reader = new IMonDBReader(emf);
		List<Instrument> results = reader.getFromCustomQuery("SELECT inst FROM Instrument inst", Instrument.class, new HashMap<>());

		assertFalse(results.isEmpty());
		Instrument instrument = results.get(0);
		assertFalse(instrument.getRunIterator().hasNext());
		assertFalse(instrument.getEventIterator().hasNext());
		assertFalse(instrument.getPropertyIterator().hasNext());
	}
}
