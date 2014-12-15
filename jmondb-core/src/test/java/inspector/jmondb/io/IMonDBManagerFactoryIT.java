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

import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

public class IMonDBManagerFactoryTest {

	@Test
	public void createMySQLFactory_valid() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test
	public void createMySQLFactory_ipHost() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("127.0.0.1", "3306", "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test
	public void createMySQLFactory_nullHost() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory(null, "3306", "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test
	public void createMySQLFactory_nullPort() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", null, "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test(expected = NullPointerException.class)
	public void createMySQLFactory_nullDatabase() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", null, "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test(expected = NullPointerException.class)
	public void createMySQLFactory_nullUser() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", "iMonDBtest", null, null);
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test(expected = PersistenceException.class)
	public void createMySQLFactory_invalidHost() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("nonlocalhost", "3306", "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test(expected = PersistenceException.class)
	public void createMySQLFactory_invalidPort() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "123456", "iMonDBtest", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test(expected = PersistenceException.class)
	public void createMySQLFactory_invalidDatabase() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", "noDb", "iMonDB", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test(expected = PersistenceException.class)
	public void createMySQLFactory_invalidUser() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", "iMonDBtest", "noUser", "iMonDB");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test(expected = PersistenceException.class)
	public void createMySQLFactory_invalidPassword() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", "3306", "iMonDBtest", "iMonDB", "wrongPass");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}
}
