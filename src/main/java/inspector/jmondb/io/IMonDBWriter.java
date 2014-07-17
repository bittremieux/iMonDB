package inspector.jmondb.io;

import inspector.jmondb.model.CvTerm;
import inspector.jmondb.model.Project;
import inspector.jmondb.model.Property;
import inspector.jmondb.model.Run;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An iMonDB output writer to write to an RDBMS.
 */
public class IMonDBWriter {

	private static final Logger logger = LogManager.getLogger(IMonDBWriter.class);

	/** EntityManagerFactory used to set up connections to the database */
	private EntityManagerFactory emf;

	/**
	 * Creates an IMonDBWriter specified by the given {@link EntityManagerFactory}.
	 *
	 * @param emf  the EntityManagerFactory used to set up the connection to the database
	 */
	public IMonDBWriter(EntityManagerFactory emf) {
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
	 * Persist the given {@link Project} to the database.
	 *
	 * If a Project with the same label was already present, the previous Project is replaced by the given Project.
	 *
	 * @param project  The Project that will be persisted to the database
	 */
	public void writeProject(Project project) {
		if(project != null) {
			logger.info("Store project <{}>", project.getLabel());

			EntityManager entityManager = createEntityManager();

			// persist the project in a transaction
			try {
				// check if the project is already in the database and if so, delete the old information
				// (the delete will cascade to the child Runs and Values)
				TypedQuery<Long> projectQuery = entityManager.createQuery("SELECT project.id FROM Project project WHERE project.label = :label", Long.class);
				projectQuery.setParameter("label", project.getLabel());
				projectQuery.setMaxResults(1);	// restrict to a single result (label is unique anyway)

				List<Long> result = projectQuery.getResultList();
				if(result.size() > 0) {
					// delete the old project
					logger.info("Duplicate project <label={}>: delete old project <id={}>", project.getLabel(), result.get(0));
					Project oldProject = entityManager.find(Project.class, result.get(0));
					entityManager.getTransaction().begin();
					entityManager.remove(oldProject);
					entityManager.getTransaction().commit();
				}

				// store the new project
				entityManager.getTransaction().begin();
				entityManager.merge(project);
				entityManager.getTransaction().commit();
			}
			catch(EntityExistsException e) {
				try {
					entityManager.getTransaction().rollback();
				}
				catch(PersistenceException p) {
					logger.error("Unable to rollback the transaction for project <{}> to the database: {}", project.getLabel(), p);
				}

				logger.error("Unable to persist project <{}> to the database: {}", project.getLabel(), e);
				throw new IllegalArgumentException("Unable to persist project <" + project.getLabel() + "> to the database");
			}
			catch(RollbackException e) {
				logger.error("Unable to commit the transaction for project <{}> to the database: {}", project.getLabel(), e);
				throw new IllegalArgumentException("Unable to commit the transaction for project <" + project.getLabel() + "> to the database");
			}
			finally {
				entityManager.close();
			}
		}
		else {
			logger.error("Unable to write <null> project element to the database");
			throw new NullPointerException("Unable to write <null> project element to the database");
		}
	}

	/**
	 * Persist the given {@link Run} to the database.
	 *
	 * If a Run with the same name was already present, the previous Run is replaced by the given Run.
	 *
	 * @param run  The Run that will be persisted to the database
	 */
	public void writeRun(Run run) {
		if(run != null) {
			logger.info("Store run <{}>", run.getName());

			EntityManager entityManager = createEntityManager();

			// persist the run in a transaction
			try {
				// check if the run is already in the database and if so, delete the old information
				// (the delete will cascade to the child Values)
				TypedQuery<Long> runQuery = entityManager.createQuery("SELECT run.id FROM Run run WHERE run.name = :name", Long.class);
				runQuery.setParameter("name", run.getName());
				runQuery.setMaxResults(1);	// restrict to a single result

				List<Long> result = runQuery.getResultList();
				if(result.size() > 0) {
					// delete the old run
					logger.info("Duplicate run <name={}>: delete old run <id={}>", run.getName(), result.get(0));
					Run oldRun = entityManager.find(Run.class, result.get(0));
					entityManager.getTransaction().begin();
					entityManager.remove(oldRun);
					entityManager.getTransaction().commit();
				}

				// store the new run
				entityManager.getTransaction().begin();
				entityManager.merge(run);
				entityManager.getTransaction().commit();
			}
			catch(EntityExistsException e) {
				try {
					entityManager.getTransaction().rollback();
				}
				catch(PersistenceException p) {
					logger.error("Unable to rollback the transaction for run <{}> to the database: {}", run.getName(), p);
				}

				logger.error("Unable to persist run <{}> to the database: {}", run.getName(), e);
				throw new IllegalArgumentException("Unable to persist run <" + run.getName() + "> to the database");
			}
			catch(RollbackException e) {
				logger.error("Unable to commit the transaction for run <{}> to the database: {}", run.getName(), e);
				throw new IllegalArgumentException("Unable to commit the transaction for run <" + run.getName() + "> to the database");
			}
			finally {
				entityManager.close();
			}
		}
		else {
			logger.error("Unable to write <null> run element to the database");
			throw new NullPointerException("Unable to write <null> run element to the database");
		}
	}

	public void writeProperty(Property property) {
		if(property != null) {
			logger.info("Store property <{}>", property.getName());

			EntityManager entityManager = createEntityManager();

			// persist the Property in a transaction
			try {
				// check if the Property is already in the database and retrieve its primary key
				TypedQuery<Long> query = entityManager.createQuery("SELECT property.id FROM Property property WHERE property.name = :name", Long.class);
				query.setParameter("name", property.getName()).setMaxResults(1);
				List<Long> result = query.getResultList();
				if(result.size() > 0) {
					logger.info("Duplicate property <name={}>: assign id <{}>", property.getName(), result.get(0));
					property.setId(result.get(0));
				}

				// store this Property
				entityManager.getTransaction().begin();
				entityManager.merge(property);
				entityManager.getTransaction().commit();
			}
			catch(EntityExistsException e) {
				try {
					entityManager.getTransaction().rollback();
				}
				catch(PersistenceException p) {
					logger.error("Unable to rollback the transaction for property <{}> to the database: {}", property.getName(), p);
				}

				logger.error("Unable to persist property <{}> to the database: {}", property.getName(), e);
				throw new IllegalArgumentException("Unable to persist property <" + property.getName() + "> to the database");
			}
			catch(RollbackException e) {
				logger.error("Unable to commit the transaction for property <{}> to the database: {}", property.getName(), e);
				throw new IllegalArgumentException("Unable to commit the transaction for property <" + property.getName() + "> to the database");
			}
			finally {
				entityManager.close();
			}
		}

		else {
			logger.error("Unable to write <null> property to the database");
			throw new NullPointerException("Unable to write <null> property to the database");
		}
	}

	public void writeCvTerm(CvTerm cvTerm) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
