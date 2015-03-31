package inspector.imondb.io;

/*
 * #%L
 * iMonDB Core
 * %%
 * Copyright (C) 2014 - 2015 InSPECtor
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

import inspector.imondb.convert.thermo.ThermoRawFileExtractor;
import inspector.imondb.model.CV;
import inspector.imondb.model.Instrument;
import inspector.imondb.model.InstrumentModel;
import inspector.imondb.model.Run;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ThermoRawFileExtractorIT {

	private static final String PORT = System.getProperty("mysql.port");

	private EntityManagerFactory emf;

	@Before
	public void setUp() {
		System.clearProperty("exclusion.properties");

		emf = IMonDBManagerFactory.createMySQLFactory("localhost", PORT, "root", "root", "root");
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

	@Test(expected = IllegalArgumentException.class)
	public void init_nonExistingExclusionProperties() {
		System.setProperty("exclusion.properties", "non/existing/file");
		new ThermoRawFileExtractor();
	}

	@Test
	public void init_existingExclusionProperties() {
		System.setProperty("exclusion.properties", loadResource("/exclusion.properties").getAbsolutePath());
		new ThermoRawFileExtractor();
	}

	@Test(expected = NullPointerException.class)
	public void extract_nullFile() {
		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		extractor.extractInstrumentData(null, "run", new Instrument("instrument", InstrumentModel.UNKNOWN_MODEL, new CV("cv", "name", "uri", "1")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void extract_nonRawFile() {
		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		extractor.extractInstrumentData(loadResource("/attachment.jpg").getAbsolutePath(), "run",
                new Instrument("instrument", InstrumentModel.UNKNOWN_MODEL, new CV("cv", "name", "uri", "1")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void extract_nonExistingFile() {
		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		extractor.extractInstrumentData("non existing file.raw", "run", new Instrument("instrument", InstrumentModel.UNKNOWN_MODEL, new CV("cv", "name", "uri", "1")));
	}

	@Test(expected = NullPointerException.class)
	public void extract_illegalRawFile() {
		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		extractor.extractInstrumentData(loadResource("/IllegalFile.raw").getAbsolutePath(), null, null);
	}

	@Test(expected = NullPointerException.class)
	public void extract_nullInstrument() {
		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		extractor.extractInstrumentData(loadResource("/LtqOrbitrap.raw").getAbsolutePath(), null, null);
	}

	@Test
	public void extractLtqOrbitrap() {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Instrument instrument = new Instrument("test instrument", InstrumentModel.THERMO_LTQ_ORBITRAP, new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0"));
		writer.writeInstrument(instrument);

		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		Run run = extractor.extractInstrumentData(loadResource("/LtqOrbitrap.raw").getAbsolutePath(), null, instrument);

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
		Run run = extractor.extractInstrumentData(loadResource("/OrbitrapXL.raw").getAbsolutePath(), null, instrument);

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
		Run run = extractor.extractInstrumentData(loadResource("/LtqVelos.raw").getAbsolutePath(), null, instrument);

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
		Run run = extractor.extractInstrumentData(loadResource("/TsqVantage.raw").getAbsolutePath(), null, instrument);

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
		Run run = extractor.extractInstrumentData(loadResource("/OrbitrapVelos.raw").getAbsolutePath(), null, instrument);

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
		Run run = extractor.extractInstrumentData(loadResource("/QExactive.raw").getAbsolutePath(), null, instrument);

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
		Run run = extractor.extractInstrumentData(loadResource("/OrbitrapFusion.raw").getAbsolutePath(), null, instrument);

		writer.writeRun(run);

		IMonDBReader reader = new IMonDBReader(emf);
		Run runDb = reader.getRun("OrbitrapFusion", instrument.getName());

		// compare all elements
		assertEquals(run, runDb);
	}

	private File loadResource(String fileName) {
		try {
			return new File(getClass().getResource(fileName).toURI());
		} catch(URISyntaxException e) {
			fail(e.getMessage());
		}
		return null;
	}
}
