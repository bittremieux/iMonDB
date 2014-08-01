package inspector.jmondb.io;

import inspector.jmondb.model.Project;
import inspector.jmondb.model.Run;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

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
	 * Returns the {@link Project} specified by the given label.
	 *
	 * @param label  The label of the project
	 * @return The project specified by the given label if found, else {@code null}
	 */
	public Project getProject(String label) {
		logger.info("Retrieve project <{}>", label);

		EntityManager entityManager = createEntityManager();

		try {
			TypedQuery<Project> query = entityManager.createQuery("SELECT project FROM Project project WHERE project.label = :label", Project.class);
			query.setParameter("label", label);

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
	 * Returns the {@link Run} specified by the given name.
	 *
	 * @param name  The name of the run
	 * @param projectLabel  The label identifying the Project to which the Run belongs
	 * @return The run specified by the given name if found, else {@code null}
	 */
	public Run getRun(String name, String projectLabel) {
		logger.info("Retrieve run <{}> from project <{}>", name, projectLabel);

		EntityManager entityManager = createEntityManager();

		try {
			TypedQuery<Run> query = entityManager.createQuery("SELECT run FROM Run run WHERE run.name = :name AND run.fromProject.label = :label", Run.class);
			query.setParameter("name", name);
			query.setParameter("label", projectLabel);

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
	 * @param query  The query used to retrieve the data
	 * @param clss  The class type of the object returned by the query
	 * @param <T>  The type of the requested data
	 * @return A List containing all objects of the given type returned by the given query
	 */
	public <T> List<T> getFromCustomQuery(String query, Class<T> clss) {
		if(query != null && clss != null) {
			logger.info("Execute custom query: {}", query);

			EntityManager entityManager = createEntityManager();

			try {
				return entityManager.createQuery(query, clss).getResultList();
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
