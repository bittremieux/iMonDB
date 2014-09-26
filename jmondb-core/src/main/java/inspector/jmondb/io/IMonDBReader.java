package inspector.jmondb.io;

import inspector.jmondb.model.Property;
import inspector.jmondb.model.Run;
import inspector.jmondb.model.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An iMonDB input reader to read from an RDBMS.
 */
public class IMonDBReader {

	private static final Logger logger = LogManager.getLogger(IMonDBReader.class);

	/** {@link EntityManagerFactory} used to set up connections to the database */
	private EntityManagerFactory emf;

	/**
	 * Creates an {@code IMonDBReader} specified by the given {@link EntityManagerFactory}.
	 *
	 * @param emf  the {@code EntityManagerFactory} used to set up the connection to the database, not {@code null}
	 */
	public IMonDBReader(EntityManagerFactory emf) {
		if(emf != null)
			this.emf = emf;
		else {
			logger.error("The EntityManagerFactory is not allowed to be <null>");
			throw new NullPointerException("The EntityManagerFactory is not allowed to be <null>");
		}
	}

	/**
	 * Creates an {@link EntityManager} to set up a connection to the database.
	 *
	 * @return an {@code EntityManager} to connect to the database
	 */
	private EntityManager createEntityManager() {
		try {
			return emf.createEntityManager();
		}
		catch(Exception e) {
			logger.info("Error while creating the EntityManager to connect to the database: {}", e);
			throw new IllegalStateException("Couldn't connect to the database: " + e);
		}
	}

	/**
	 * Returns the {@link Run} specified by the given name.
	 *
	 * @param name  the name of the requested {@code Run}
	 * @return the {@code Run} specified by the given name if present in the database, else {@code null}
	 */
	public Run getRun(String name) {
		logger.info("Retrieve run <{}>", name);

		EntityManager entityManager = createEntityManager();

		try {
			TypedQuery<Run> query = entityManager.createQuery("SELECT run FROM Run run WHERE run.name = :name", Run.class);
			query.setParameter("name", name);

			// get the run
			Run run = query.getSingleResult();

			// explicitly retrieve all values and associated properties for the run
			Iterator<Value> valIt = run.getValueIterator();
			while(valIt.hasNext()) {
				Value val = valIt.next();
				val.hashCode();
				val.getDefiningProperty().hashCode();
			}

			return run;
		}
		catch(NoResultException e) {
			return null;
		}
		finally {
			entityManager.close();
		}
	}

	/**
	 * Returns the {@link Property} specified by the given name.
	 *
	 * @param name  the name of the requested {@code Property}
	 * @return the {@code Property} specified by the given name if found, else {@code null}
	 */
	public Property getProperty(String name) {
		logger.info("Retrieve property <{}>", name);

		EntityManager entityManager = createEntityManager();

		try {
			TypedQuery<Property> query = entityManager.createQuery("SELECT prop FROM Property prop WHERE prop.name = :name", Property.class);
			query.setParameter("name", name);

			// get the property
			Property prop = query.getSingleResult();

			// explicitly retrieve all values and associated runs for the property
			Iterator<Value> valIt = prop.getValueIterator();
			while(valIt.hasNext()) {
				Value val = valIt.next();
				val.hashCode();
				val.getOriginatingRun().hashCode();
			}

			return prop;
		}
		catch(NoResultException e) {
			return null;
		}
		finally {
			entityManager.close();
		}
	}

	/**
	 * Returns arbitrary data retrieved by a custom JPQL query.
	 *
	 * Use this method only for queries without any parameters.
	 * To include parameters, please use the {@link #getFromCustomQuery(String, Class, Map)} method.
	 *
	 * @param queryStr  the query used to retrieve the data, {@code null} returns {@code null}
	 * @param clss  the class type of the query result, {@code null} returns {@code null}
	 * @param <T>  the type of the requested data
	 * @return a {@code List} containing all objects of the given type returned by the given query
	 */
	public <T> List<T> getFromCustomQuery(String queryStr, Class<T> clss) {
		return getFromCustomQuery(queryStr, clss, null);
	}

	/**
	 * Returns arbitrary data retrieved by a custom JPQL query.
	 *
	 * Parameters can be inserted in the query by making use of named parameters (:name).
	 * Named parameters have to be specified by their special form, preceded by a colon, in the query string.
	 * The parameters {@code Map} contains for each parameter the name (without the colon prefix) and the value that will be substituted.
	 *
	 * @param queryStr  the query used to retrieve the data, {@code null} returns {@code null}
	 * @param clss  the class type of the query result, {@code null} returns {@code null}
	 * @param parameters  a {@code Map} of named parameters and their values
	 * @param <T>  the type of the requested data
	 * @return a {@code List} containing all objects of the given type returned by the given query
	 */
	public <T> List<T> getFromCustomQuery(String queryStr, Class<T> clss, Map<String, String> parameters) {
		if(queryStr != null && clss != null) {
			logger.info("Execute custom query: {}", queryStr);

			EntityManager entityManager = createEntityManager();

			try {
				TypedQuery<T> query = entityManager.createQuery(queryStr, clss);

				if(parameters != null)
					for(Map.Entry<String, String> entry : parameters.entrySet()) {
						logger.info("Set parameter <>: <>", entry.getKey(), entry.getValue());
						query.setParameter(entry.getKey(), entry.getValue());
					}

				return query.getResultList();
			}
			finally {
				entityManager.close();
			}
		}
		else {
			logger.info("Unable to execute <null> query");
			return null;
		}
	}
}
