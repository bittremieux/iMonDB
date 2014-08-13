package inspector.jmondb.model;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.*;

public class ProjectTest {

	private Project project;

	@Before
	public void setUp() {
		project = new Project("test", "Test project", "Project used for unit tests");

		for(int i = 0; i < 4; i++) {
			project.addRun(new Run("run_" + i, "path/to/run/" + i, new Timestamp(Calendar.getInstance().getTime().getTime())));
		}
	}

	@Test
	public void getRun_null() {
		assertNull(project.getRun(null));
	}

	@Test
	public void getRun_nonExisting() {
		assertNull(project.getRun("non-existing"));
	}

	@Test
	public void getRun_existing() {
		assertNotNull(project.getRun("run_2"));
	}

	@Test(expected=NullPointerException.class)
	public void addRun_null() {
		project.addRun(null);
	}

	@Test
	public void addRun_duplicate() {
		int nrOfRuns = project.getNumberOfRuns();
		project.addRun(new Run("run_0", "different/path", new Timestamp(Calendar.getInstance().getTime().getTime())));
		assertEquals(nrOfRuns, project.getNumberOfRuns());
		assertEquals("different/path", project.getRun("run_0").getStorageName());
	}

	@Test
	public void addRun_new() {
		int nrOfRuns = project.getNumberOfRuns();
		project.addRun(new Run("new run", "new/path", new Timestamp(Calendar.getInstance().getTime().getTime())));
		assertEquals(nrOfRuns + 1, project.getNumberOfRuns());
	}

	@Test
	public void removeRun_null() {
		int nrOfRuns = project.getNumberOfRuns();
		project.removeRun(null);
		assertEquals(nrOfRuns, project.getNumberOfRuns());
	}

	@Test
	public void removeRun_nonExisting() {
		int nrOfRuns = project.getNumberOfRuns();
		project.removeRun("non-existing run");
		assertEquals(nrOfRuns, project.getNumberOfRuns());
	}

	@Test
	public void removeRun_valid() {
		int nrOfRuns = project.getNumberOfRuns();
		project.removeRun("run_1");
		assertEquals(nrOfRuns - 1, project.getNumberOfRuns());
		assertNull(project.getRun("run_1"));
	}
}
