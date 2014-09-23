package inspector.jmondb.io;

import com.google.common.collect.ImmutableMap;
import inspector.jmondb.convert.Thermo.ThermoRawFileExtractor;
import inspector.jmondb.model.CV;
import inspector.jmondb.model.Run;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.io.*;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class IMonDBReaderWriterTest {

	private EntityManagerFactory emf;
	private IMonDBReader reader;
	private IMonDBWriter writer;
	private ThermoRawFileExtractor extractor;

	@Before
	public void setUp() {
		emf = IMonDBManagerFactory.createMySQLFactory(null, null, "iMonDBtest", "iMonDB", "iMonDB");
		reader = new IMonDBReader(emf);
		writer = new IMonDBWriter(emf);
		extractor = new ThermoRawFileExtractor();
	}

	@After
	public void tearDown() {
		EntityManager em = emf.createEntityManager();

		// clear all existing data
		em.getTransaction().begin();
		em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_value").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_cv").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_run").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE imon_project").executeUpdate();
		em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
		em.getTransaction().commit();

		emf.close();

		// reload the standard sql dump
		try {
			String[] cmd = new String[] { "mysql", "iMonDBtest", "--user=iMonDB", "--password=iMonDB", "-e", "source " + new File(getClass().getResource("/imondbtest-dump.sql").getFile()).getAbsolutePath() };
			Process process = Runtime.getRuntime().exec(cmd);

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while((line = br.readLine()) != null)
				System.out.println(line);
			br.close();

			process.waitFor();
		} catch(IOException | InterruptedException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void getRun_nullRun() {
		assertNull(reader.getRun(null));
	}

	@Test
	public void getRun_valid() {
		assertNotNull(reader.getRun("OrbitrapVelos-example"));
	}

	@Test
	public void getFromCustomQuery_nullQuery() {
		assertNull(reader.getFromCustomQuery(null, Run.class, null));
	}

	@Test
	public void getFromCustomQuery_nullClass() {
		assertNull(reader.getFromCustomQuery("SELECT proj FROM Project proj WHERE proj.label = \"Wout\"", null, null));
	}

	@Test
	public void getFromCustomQuery_nullParameters() {
		assertNotNull(reader.getFromCustomQuery("SELECT run FROM Run run WHERE run.name = \"Wout\"", Run.class, null));
	}

	@Test
	public void getFromCustomQuery() {
		Map<String, String> parameters = ImmutableMap.of("run", "Wout");
		assertNotNull(reader.getFromCustomQuery("SELECT run FROM Run run WHERE run.name = :run", Run.class, parameters));
	}

	@Test(expected = NullPointerException.class)
	public void writeRun_nullRun() {
		writer.writeRun(null);
	}

	@Test(expected = NullPointerException.class)
	public void writeCv_null() {
		writer.writeCv(null);
	}

	@Test
	public void writeCv_valid() {
		Map<String, String> parameters = ImmutableMap.of("label", "test");
		assertEquals(0, reader.getFromCustomQuery("SELECT cv FROM CV cv WHERE cv.label = :label", CV.class, parameters).size());

		CV cv = new CV("test", "Test CV", "uri/to/test/cv", "1");
		writer.writeCv(cv);

		assertEquals(1, reader.getFromCustomQuery("SELECT cv FROM CV cv WHERE cv.label = :label", CV.class, parameters).size());
	}

	@Test
	public void writeCv_duplicate() {
		Map<String, String> parameters = ImmutableMap.of("label", "test");
		assertEquals(0, reader.getFromCustomQuery("SELECT cv FROM CV cv WHERE cv.label = :label", CV.class, parameters).size());

		CV cv = new CV("test", "Test CV", "uri/to/test/cv", "1");
		writer.writeCv(cv);

		assertEquals(1, reader.getFromCustomQuery("SELECT cv FROM CV cv WHERE cv.label = :label", CV.class, parameters).size());

		CV newCv = new CV("test", "New CV", "uri/to/new/cv", "1");
		writer.writeCv(newCv);

		CV dbCv = reader.getFromCustomQuery("SELECT cv FROM CV cv WHERE cv.label = :label", CV.class, parameters).get(0);
		assertNotNull(dbCv);
		assertEquals("New CV", dbCv.getName());
	}

	@Test
	public void writeReadLtqOrbitrap() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/LtqOrbitrap.raw").getFile()).getAbsolutePath());

		writer.writeRun(run);

		Run runDb = reader.getRun("LtqOrbitrap");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadOrbitrapXL() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/OrbitrapXL.raw").getFile()).getAbsolutePath());

		writer.writeRun(run);

		Run runDb = reader.getRun("OrbitrapXL");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadLtqVelos() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/LtqVelos.raw").getFile()).getAbsolutePath());

		writer.writeRun(run);

		Run runDb = reader.getRun("LtqVelos");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadTsqVantage() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/TsqVantage.raw").getFile()).getAbsolutePath());

		writer.writeRun(run);

		Run runDb = reader.getRun("TsqVantage");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadOrbitrapVelos() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/OrbitrapVelos.raw").getFile()).getAbsolutePath());

		writer.writeRun(run);

		Run runDb = reader.getRun("OrbitrapVelos");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadQExactive() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/QExactive.raw").getFile()).getAbsolutePath());

		writer.writeRun(run);

		Run runDb = reader.getRun("QExactive");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadOrbitrapFusion() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/OrbitrapFusion.raw").getFile()).getAbsolutePath());

		writer.writeRun(run);

		Run runDb = reader.getRun("OrbitrapFusion");

		// compare all elements
		assertEquals(run, runDb);
	}
}
