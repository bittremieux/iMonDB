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
	public void extractLcqDecaXpPlus() {
		testExtractor(InstrumentModel.THERMO_LCQ_DECA_XP_PLUS, "/LcqDecaXpPlus.raw");
	}

	@Test
	public void extractLtqFt() {
		testExtractor(InstrumentModel.THERMO_LTQ_FT, "/LtqFt.raw");
	}

	@Test
	public void extractLtqOrbitrap() {
		testExtractor(InstrumentModel.THERMO_LTQ_ORBITRAP, "/LtqOrbitrap.raw");
	}

	@Test
	public void extractLtqOrbitrapDiscovery() {
		testExtractor(InstrumentModel.THERMO_LTQ_ORBITRAP_DISCOVERY, "/LtqOrbitrapDiscovery.raw");
	}

	@Test
	public void extractOrbitrapXL() {
		testExtractor(InstrumentModel.THERMO_ORBITRAP_XL, "/OrbitrapXL.raw");
	}

	@Test
	public void extractLtqFtUltra() {
		testExtractor(InstrumentModel.THERMO_LTQ_FT_ULTRA, "/LtqFtUltra.raw");
	}

	@Test
	public void extractLtqVelos() {
		testExtractor(InstrumentModel.THERMO_LTQ_VELOS, "/LtqVelos.raw");
	}

	@Test
	public void extractTsqVantage() {
		testExtractor(InstrumentModel.THERMO_TSQ_VANTAGE, "/TsqVantage.raw");
	}

	@Test
	public void extractOrbitrapVelos() {
		testExtractor(InstrumentModel.THERMO_ORBITRAP_VELOS, "/OrbitrapVelos.raw");
	}

	@Test
	public void extractLtqOrbitrapElite() {
		testExtractor(InstrumentModel.THERMO_LTQ_ORBITRAP_ELITE, "/LtqOrbitrapElite.raw");
	}

	@Test
	public void extractQExactive() {
		testExtractor(InstrumentModel.THERMO_Q_EXACTIVE, "/QExactive.raw");
	}

	@Test
	public void extractOrbitrapFusion() {
		testExtractor(InstrumentModel.THERMO_ORBITRAP_FUSION, "/OrbitrapFusion.raw");
	}

	private File loadResource(String fileName) {
		try {
			return new File(getClass().getResource(fileName).toURI());
		} catch(URISyntaxException e) {
			fail(e.getMessage());
		}
		return null;
	}

	private void testExtractor(InstrumentModel model, String fileName) {
		IMonDBWriter writer = new IMonDBWriter(emf);
		Instrument instrument = new Instrument("test instrument", model, new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0"));
		writer.writeInstrument(instrument);

		ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
		Run run = extractor.extractInstrumentData(loadResource(fileName).getAbsolutePath(), null, instrument);

		writer.writeRun(run);

		IMonDBReader reader = new IMonDBReader(emf);
		String runName = fileName.substring(fileName.indexOf('/') + 1, fileName.indexOf(".raw"));
		Run runDb = reader.getRun(runName, instrument.getName());

		// compare all elements
		assertEquals(run, runDb);
	}
}
