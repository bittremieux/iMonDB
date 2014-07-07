package inspector.jmondb.io;

import inspector.jmondb.model.Project;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

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
	 * @return  The project specified by the given label if found, else {@code null}
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
}
