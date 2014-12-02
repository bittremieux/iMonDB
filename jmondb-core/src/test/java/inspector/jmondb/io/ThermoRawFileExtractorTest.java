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

import inspector.jmondb.convert.Thermo.ThermoRawFileExtractor;
import inspector.jmondb.model.CV;
import inspector.jmondb.model.Instrument;
import inspector.jmondb.model.InstrumentModel;
import inspector.jmondb.model.Run;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class ThermoRawFileExtractorTest {

	private EntityManagerFactory emf;

	@Before
	public void setUp() {
		emf = IMonDBManagerFactory.createMySQLFactory(null, null, "iMonDBtest", "iMonDB", "iMonDB");
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

	@Test
	public void extractLtqOrbitrap() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Instrument instrument = new Instrument("test instrument", InstrumentModel.THERMO_LTQ_ORBITRAP, new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0"));
		writer.writeInstrument(instrument);

		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/LtqOrbitrap.raw").getFile()).getAbsolutePath(), null, instrument.getName());

		writer.writeRun(run);

		IMonDBReader reader = new IMonDBReader(emf);
		Run runDb = reader.getRun("LtqOrbitrap", instrument.getName());

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void extractOrbitrapXL() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Instrument instrument = new Instrument("test instrument", InstrumentModel.THERMO_ORBITRAP_XL, new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0"));
		writer.writeInstrument(instrument);

		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/OrbitrapXL.raw").getFile()).getAbsolutePath(), null, instrument.getName());

		writer.writeRun(run);

		IMonDBReader reader = new IMonDBReader(emf);
		Run runDb = reader.getRun("OrbitrapXL", instrument.getName());

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void extractLtqVelos() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Instrument instrument = new Instrument("test instrument", InstrumentModel.THERMO_LTQ_VELOS, new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0"));
		writer.writeInstrument(instrument);

		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/LtqVelos.raw").getFile()).getAbsolutePath(), null, instrument.getName());

		writer.writeRun(run);

		IMonDBReader reader = new IMonDBReader(emf);
		Run runDb = reader.getRun("LtqVelos", instrument.getName());

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void extractTsqVantage() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Instrument instrument = new Instrument("test instrument", InstrumentModel.THERMO_TSQ_VANTAGE, new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0"));
		writer.writeInstrument(instrument);

		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/TsqVantage.raw").getFile()).getAbsolutePath(), null, instrument.getName());

		writer.writeRun(run);

		IMonDBReader reader = new IMonDBReader(emf);
		Run runDb = reader.getRun("TsqVantage", instrument.getName());

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void extractOrbitrapVelos() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Instrument instrument = new Instrument("test instrument", InstrumentModel.THERMO_ORBITRAP_VELOS, new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0"));
		writer.writeInstrument(instrument);

		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/OrbitrapVelos.raw").getFile()).getAbsolutePath(), null, instrument.getName());

		writer.writeRun(run);

		IMonDBReader reader = new IMonDBReader(emf);
		Run runDb = reader.getRun("OrbitrapVelos", instrument.getName());

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void extractQExactive() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Instrument instrument = new Instrument("test instrument", InstrumentModel.THERMO_Q_EXACTIVE, new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0"));
		writer.writeInstrument(instrument);

		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/QExactive.raw").getFile()).getAbsolutePath(), null, instrument.getName());

		writer.writeRun(run);

		IMonDBReader reader = new IMonDBReader(emf);
		Run runDb = reader.getRun("QExactive", instrument.getName());

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void extractOrbitrapFusion() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Instrument instrument = new Instrument("test instrument", InstrumentModel.THERMO_ORBITRAP_FUSION, new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0"));
		writer.writeInstrument(instrument);

		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/OrbitrapFusion.raw").getFile()).getAbsolutePath(), null, instrument.getName());

		writer.writeRun(run);

		IMonDBReader reader = new IMonDBReader(emf);
		Run runDb = reader.getRun("OrbitrapFusion", instrument.getName());

		// compare all elements
		assertEquals(run, runDb);
	}
}
