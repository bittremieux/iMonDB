package inspector.jmondb.viewer.model;

import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBReader;
import inspector.jmondb.io.IMonDBWriter;

import javax.persistence.EntityManagerFactory;

public class DatabaseConnection {

    private static DatabaseConnection connection = null;

    private EntityManagerFactory emf;
    private IMonDBReader reader;
    private IMonDBWriter writer;

    private DatabaseConnection() {

    }

    public static DatabaseConnection getConnection() {
        if(connection == null) {
            connection = new DatabaseConnection();
        }
        return connection;
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
        emf = IMonDBManagerFactory.createMySQLFactory(host, port, database, userName, password);
        reader = new IMonDBReader(emf);
        writer = new IMonDBWriter(emf);
    }
}
