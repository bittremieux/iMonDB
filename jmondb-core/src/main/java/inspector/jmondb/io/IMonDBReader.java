package inspector.jmondb.io;

import inspector.jmondb.model.Run;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;

/**
 * An iMonDB input reader to read from an RDBMS.
 */
public class IMonDBReader {

	private static final Logger logger = LogManager.getLogger(IMonDBReader.class);

	/** EntityManagerFactory used to set up connections to the database */
	private EntityManagerFactory emf;

	/**
	 * Creates an IMonDBReader specified by the given {@link EntityManagerFactory}.
	 *
	 * @param emf  The EntityManagerFactory used to set up the connection to the database
	 */
	public IMonDBReader(EntityManagerFactory emf) {
		this.emf = emf;
	}

	/**
	 * Creates an {@link EntityManager} to set up a connection to the database.
	 *
	 * @return An EntityManager to connect to the database
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
	 * @param name  The name of the run
	 * @return The run specified by the given name if found, else {@code null}
	 */
	public Run getRun(String name) {
		logger.info("Retrieve run <{}>", name);

		EntityManager entityManager = createEntityManager();

		try {
			TypedQuery<Run> query = entityManager.createQuery("SELECT run FROM Run run WHERE run.name = :name", Run.class);
			query.setParameter("name", name);

			return query.getSingleResult();
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
	 * @param queryStr  The query used to retrieve the data
	 * @param clss  The class type of the object returned by the query
	 * @param <T>  The type of the requested data
	 * @return A List containing all objects of the given type returned by the given query
	 */
	public <T> List<T> getFromCustomQuery(String queryStr, Class<T> clss) {
		return getFromCustomQuery(queryStr, clss, null);
	}

	/**
	 * Returns arbitrary data retrieved by a custom JPQL query.
	 *
	 * Parameters can be inserted in the query by making use of named parameters (:name).
	 * Named parameters have to be specified by their special form, preceded by a colon, in the query string.
	 * The parameters Map contains for each parameter the name (without the colon prefix) and the value that will be substituted.
	 *
	 * @param queryStr  The query used to retrieve the data
	 * @param clss  The class type of the object returned by the query
	 * @param parameters  A Map of named parameters and their values
	 * @param <T>  The type of the requested data
	 * @return A List containing all objects of the given type returned by the given query
	 */
	public <T> List<T> getFromCustomQuery(String queryStr, Class<T> clss, Map<String, String> parameters) {
		if(queryStr != null && clss != null) {
			logger.info("Execute custom query: {}", queryStr);

			EntityManager entityManager = createEntityManager();

			try {
				TypedQuery<T> query = entityManager.createQuery(queryStr, clss);

				if(parameters != null)
					for(Map.Entry<String, String> entry : parameters.entrySet())
						query.setParameter(entry.getKey(), entry.getValue());

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
