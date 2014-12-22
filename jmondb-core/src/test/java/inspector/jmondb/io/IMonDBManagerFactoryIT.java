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

public class IMonDBManagerFactoryIT {

	private static final String PORT = System.getProperty("mysql.port");

	@Test
	public void createMySQLFactory_valid() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", PORT, "root", "root", "root");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test
	public void createMySQLFactory_ipHost() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("127.0.0.1", PORT, "root", "root", "root");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test
	public void createMySQLFactory_nullHost() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory(null, PORT, "root", "root", "root");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test(expected = NullPointerException.class)
	public void createMySQLFactory_nullDatabase() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", PORT, null, "root", "root");
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}

	@Test(expected = NullPointerException.class)
	public void createMySQLFactory_nullUser() {
		// create EMF
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory("localhost", PORT, "root", null, null);
		// test connection
		IMonDBReader reader = new IMonDBReader(emf);
		reader.getInstrument(null);
		// close emf
		emf.close();
	}
}
