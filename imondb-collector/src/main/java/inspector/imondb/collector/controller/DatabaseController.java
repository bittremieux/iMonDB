package inspector.imondb.collector.controller;

/*
 * #%L
 * iMonDB Collector
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

import inspector.imondb.io.IMonDBManagerFactory;
import inspector.imondb.io.IMonDBReader;
import inspector.imondb.io.IMonDBWriter;

import javax.persistence.EntityManagerFactory;

public class DatabaseController {

    private EntityManagerFactory emf;
    private IMonDBReader reader;
    private IMonDBWriter writer;

    public DatabaseController() {
    }

    public boolean isActive() {
        return emf != null && reader != null && writer != null;
    }

    public IMonDBReader getReader() {
        return reader;
    }

    public IMonDBWriter getWriter() {
        return writer;
    }

    public void disconnect() {
        if(emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
        reader = null;
        writer = null;
    }

    public void connectTo(String host, String port, String database, String userName, String password) {
        // first close an existing connection
        disconnect();

        // establish the new connection
        emf = IMonDBManagerFactory.createMySQLFactory(host, port, database, userName, password);
        reader = new IMonDBReader(emf);
        writer = new IMonDBWriter(emf);
    }
}
