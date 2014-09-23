package inspector.jmondb.io;

import org.junit.Test;

import javax.persistence.EntityManagerFactory;

public class IMonDBManagerFactoryTest {

	@Test
	public void createMySQLFactory_valid() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

	@Test
	public void createMySQLFactory_ipHost() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("127.0.0.1", "3306", "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

	@Test
	public void createMySQLFactory_nullHost() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory(null, "3306", "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

	@Test
	public void createMySQLFactory_nullPort() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", null, "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

	@Test(expected = NullPointerException.class)
	public void createMySQLFactory_nullDatabase() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", null, "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

	@Test(expected = NullPointerException.class)
	public void createMySQLFactory_nullUser() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", "iMonDBtest", null, null);
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

	@Test(expected = IllegalStateException.class)
	public void createMySQLFactory_invalidHost() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("nonlocalhost", "3306", "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

	@Test(expected = IllegalStateException.class)
	public void createMySQLFactory_invalidPort() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "123456", "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

	@Test(expected = IllegalStateException.class)
	public void createMySQLFactory_invalidDatabase() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", "noDb", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

	@Test(expected = IllegalStateException.class)
	public void createMySQLFactory_invalidUser() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", "iMonDBtest", "noUser", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

	@Test(expected = IllegalStateException.class)
	public void createMySQLFactory_invalidPassword() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", "iMonDBtest", "iMonDB", "wrongPass");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getRun("Wout");
		// close emf
		emf.close();
	}

}
