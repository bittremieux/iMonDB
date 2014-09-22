package inspector.jmondb.io;

import inspector.jmondb.convert.Thermo.ThermoRawFileExtractor;
import inspector.jmondb.model.CV;
import inspector.jmondb.model.Project;
import inspector.jmondb.model.Run;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.io.*;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.hamcrest.Matchers.greaterThan;
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
	public void getProject_null() {
		assertNull(reader.getProject(null));
	}

	@Test
	public void getProject_valid() {
		assertNotNull(reader.getProject("Wout"));
	}

	@Test
	public void getRun_nullProject() {
		assertNull(reader.getRun("test", null));
	}

	@Test
	public void getRun_nullRun() {
		assertNull(reader.getRun(null, "Wout"));
	}

	@Test
	public void getRun_valid() {
		assertNotNull(reader.getRun("OrbitrapVelos-example", "Wout"));
	}

	@Test
	public void getFromCustomQuery_nullQuery() {
		assertNull(reader.getFromCustomQuery(null, Project.class));
	}

	@Test
	public void getFromCustomQuery_nullClass() {
		assertNull(reader.getFromCustomQuery("SELECT proj FROM PROJECT proj WHERE proj.label = \"Wout\"", null));
	}

	@Test(expected = NullPointerException.class)
	public void writeProject_null() {
		writer.writeProject(null);
	}

	@Test
	public void writeProject_duplicate() {
		assertThat(reader.getProject("Wout").getNumberOfRuns(), greaterThan(0));

		Project project = new Project("Wout", "Different project", "Hello, this is a description");
		writer.writeProject(project);

		assertEquals(0, reader.getProject("Wout").getNumberOfRuns());
	}

	@Test
	public void writeProject_valid() {
		assertNull(reader.getProject("test"));

		Project project = new Project("test", "Test project", "Hello, this is a description");
		writer.writeProject(project);

		assertNotNull(reader.getProject("test"));
	}

	@Test
	public void writeRun_duplicate() {
		assertNull(reader.getProject("test"));

		Project project = new Project("test", "Test project", "Hello, this is a description");
		writer.writeProject(project);

		assertNotNull(reader.getProject("test"));

		Project newProject = new Project("test", "New test project", "This is another description");
		writer.writeProject(newProject);

		Project dbProject = reader.getProject("test");
		assertNotNull(dbProject);
		assertEquals("test", dbProject.getLabel());
	}

	@Test(expected = NullPointerException.class)
	public void writeRun_nullRun() {
		writer.writeRun(null, "Test project");
	}

	@Test(expected = NullPointerException.class)
	public void writeRun_nullProjectLabel() {
		Run run = new Run("test", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()));

		writer.writeRun(run, null);
	}

	@Test
	public void writeRun_existingProjectNewRun() {
		Run run = new Run("test", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()));

		assertNull(reader.getRun(run.getName(), "Wout"));

		writer.writeRun(run, "Wout");

		assertNotNull(reader.getRun(run.getName(), "Wout"));
	}

	@Test
	public void writeRun_existingProjectDuplicateRun() {
		Run run = new Run("test", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()));

		assertNull(reader.getRun(run.getName(), "Wout"));

		writer.writeRun(run, "Wout");

		assertNotNull(reader.getRun(run.getName(), "Wout"));

		Run newRun = new Run("test", "other/path/", new Timestamp(Calendar.getInstance().getTime().getTime()));

		writer.writeRun(newRun, "Wout");

		assertNotNull(reader.getRun(run.getName(), "Wout"));
		assertEquals("other/path/", reader.getRun(run.getName(), "Wout").getStorageName());
	}

	@Test
	public void writeRun_nonExistingProject() {
		Run run = new Run("test", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()));

		assertNull(reader.getProject("test"));
		assertNull(reader.getRun(run.getName(), "test"));

		writer.writeRun(run, "test");

		assertNotNull(reader.getProject("test"));
		assertNotNull(reader.getRun(run.getName(), "test"));
	}

	@Test(expected = NullPointerException.class)
	public void writeCv_null() {
		writer.writeCv(null);
	}

	@Test
	public void writeCv_valid() {
		assertEquals(0, reader.getFromCustomQuery("SELECT cv FROM CV cv WHERE cv.label = \"test\"", CV.class).size());

		CV cv = new CV("test", "Test CV", "uri/to/test/cv", "1");
		writer.writeCv(cv);

		assertEquals(1, reader.getFromCustomQuery("SELECT cv FROM CV cv WHERE cv.label = \"test\"", CV.class).size());
	}

	@Test
	public void writeCv_duplicate() {
		assertEquals(0, reader.getFromCustomQuery("SELECT cv FROM CV cv WHERE cv.label = \"test\"", CV.class).size());

		CV cv = new CV("test", "Test CV", "uri/to/test/cv", "1");
		writer.writeCv(cv);

		assertEquals(1, reader.getFromCustomQuery("SELECT cv FROM CV cv WHERE cv.label = \"test\"", CV.class).size());

		CV newCv = new CV("test", "New CV", "uri/to/new/cv", "1");
		writer.writeCv(newCv);

		CV dbCv = reader.getFromCustomQuery("SELECT cv FROM CV cv WHERE cv.label = \"test\"", CV.class).get(0);
		assertNotNull(dbCv);
		assertEquals("New CV", dbCv.getName());
	}

	@Test
	public void writeReadLtqOrbitrap() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/LtqOrbitrap.raw").getFile()).getAbsolutePath());

		writer.writeRun(run, "Wout");

		Run runDb = reader.getRun("LtqOrbitrap", "Wout");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadOrbitrapXL() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/OrbitrapXL.raw").getFile()).getAbsolutePath());

		writer.writeRun(run, "Wout");

		Run runDb = reader.getRun("OrbitrapXL", "Wout");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadLtqVelos() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/LtqVelos.raw").getFile()).getAbsolutePath());

		writer.writeRun(run, "Wout");

		Run runDb = reader.getRun("LtqVelos", "Wout");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadTsqVantage() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/TsqVantage.raw").getFile()).getAbsolutePath());

		writer.writeRun(run, "Wout");

		Run runDb = reader.getRun("TsqVantage", "Wout");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadOrbitrapVelos() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/OrbitrapVelos.raw").getFile()).getAbsolutePath());

		writer.writeRun(run, "Wout");

		Run runDb = reader.getRun("OrbitrapVelos", "Wout");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadQExactive() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/QExactive.raw").getFile()).getAbsolutePath());

		writer.writeRun(run, "Wout");

		Run runDb = reader.getRun("QExactive", "Wout");

		// compare all elements
		assertEquals(run, runDb);
	}

	@Test
	public void writeReadOrbitrapFusion() {
		Run run = extractor.extractInstrumentData(new File(getClass().getResource("/OrbitrapFusion.raw").getFile()).getAbsolutePath());

		writer.writeRun(run, "Wout");

		Run runDb = reader.getRun("OrbitrapFusion", "Wout");

		// compare all elements
		assertEquals(run, runDb);
	}
}
