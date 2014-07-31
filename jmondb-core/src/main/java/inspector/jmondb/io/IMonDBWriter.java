package inspector.jmondb.io;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import inspector.jmondb.model.CV;
import inspector.jmondb.model.Project;
import inspector.jmondb.model.Run;
import inspector.jmondb.model.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
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

				// make sure the existing cv's are retained
				for(Iterator<Run> it = project.getRunIterator(); it.hasNext(); )
					replaceDuplicateCV(it.next(), entityManager);

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
	 * Make sure duplicate cv's aren't persisted multiple times to the database.
	 *
	 * If a cv is already present in the database, set its id to the new cv, so the same cv will be retained (but updated information will be overwritten).
	 *
	 * @param run  The run for which cv's linked to all values will be checked
	 * @param entityManager  The connection to the database
	 */
	private void replaceDuplicateCV(Run run, EntityManager entityManager) {
		// get all cv's existing in the database
		TypedQuery<CV> cvQuery = entityManager.createQuery("SELECT cv FROM CV cv", CV.class);
		Map<String, CV> cvLabelMap = Maps.uniqueIndex(cvQuery.getResultList(), new Function<CV, String>() {
			public String apply(CV from) {
				return from.getLabel();
			}
		});
		// check which cv's are duplicate
		for(Iterator<Value> valIt = run.getValueIterator(); valIt.hasNext(); ) {
			CV cv = valIt.next().getCv();
			if(cv.getId() == null && cvLabelMap.containsKey(cv.getLabel())) {
				cv.setId(cvLabelMap.get(cv.getLabel()).getId());
				logger.info("Duplicate CV <label={}>: assign id <{}>", cv.getLabel(), cv.getId());
			}
		}
	}

	/**
	 * Persist the given {@link Run} to the database for the {@link Project} with the given label.
	 *
	 * If a Run with the same name was already present in this Project, the previous Run is replaced by the given Run.
	 *
	 * @param run  The Run that will be persisted to the database
	 * @param projectLabel  The label identifying the Project to which this Run will be added.
	 *                      If this project is not present in the database yet, a minimal project will be created using the given information.
	 */
	public void writeRun(Run run, String projectLabel) {
		if(run != null) {
			logger.info("Store run <{}> for project <{}>", run.getName(), projectLabel);

			EntityManager entityManager = createEntityManager();

			// persist the run in a transaction
			try {
				// check if a project with the given label is already in the database
				TypedQuery<Long> projectQuery = entityManager.createQuery("SELECT project.id FROM Project project WHERE project.label = :label", Long.class);
				projectQuery.setParameter("label", projectLabel);
				projectQuery.setMaxResults(1);	// restrict to a single result (label is unique anyway)

				List<Long> result = projectQuery.getResultList();
				if(result.size() > 0) {
					logger.info("Project <label={}> found <id={}>", projectLabel, result.get(0));

					entityManager.getTransaction().begin();

					Project project = entityManager.find(Project.class, result.get(0));
					// check if a run with the same name already existed for this project
					if(project.getRun(run.getName()) != null) {
						// if so, delete this run from the database
						logger.info("Duplicate run <name={}>: delete old run <id={}>", run.getName(), project.getRun(run.getName()).getId());
						entityManager.remove(project.getRun(run.getName()));
					}

					// make sure the existing cv's are retained
					replaceDuplicateCV(run, entityManager);

					// add the run to the prior existing project
					// the project is still attached to the entity manager, so changes are persisted on the fly
					project.addRun(run);

					entityManager.getTransaction().commit();
				}
				else {	// no project found -> create a new project
					logger.info("No existing project with label <{}> found: creating a new project. " +
							"You might want to update the information for this project", projectLabel);
					Project project = new Project(projectLabel, projectLabel);
					project.addRun(run);
					// store the complete project in the database
					writeProject(project);
				}
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

	/**
	 * Persist the given {@link CV} to the database.
	 *
	 * If a CV with the same label was already present in the database, its data will be updated to the given CV.
	 *
	 * @param cv  The cv that will be persisted to the database
	 */
	public void writeCv(CV cv) {
		if(cv != null) {
			logger.info("Store cv <{}>", cv.getLabel());

			EntityManager entityManager = createEntityManager();

			// persist the CV in a transaction
			try {
				// check if the CV is already in the database and retrieve its primary key
				TypedQuery<Long> query = entityManager.createQuery("SELECT cv.id FROM CV cv WHERE cv.label = :label", Long.class);
				query.setParameter("label", cv.getLabel()).setMaxResults(1);
				List<Long> result = query.getResultList();
				if(result.size() > 0) {
					logger.info("Duplicate cv <label={}>: assign id <{}>", cv.getLabel(), result.get(0));
					cv.setId(result.get(0));
				}

				// store this Property
				entityManager.getTransaction().begin();
				entityManager.merge(cv);
				entityManager.getTransaction().commit();
			}
			catch(EntityExistsException e) {
				try {
					entityManager.getTransaction().rollback();
				}
				catch(PersistenceException p) {
					logger.error("Unable to rollback the transaction for cv <{}> to the database: {}", cv.getLabel(), p);
				}

				logger.error("Unable to persist cv <{}> to the database: {}", cv.getLabel(), e);
				throw new IllegalArgumentException("Unable to persist cv <" + cv.getLabel() + "> to the database");
			}
			catch(RollbackException e) {
				logger.error("Unable to commit the transaction for cv <{}> to the database: {}", cv.getLabel(), e);
				throw new IllegalArgumentException("Unable to commit the transaction for cv <" + cv.getLabel() + "> to the database");
			}
			finally {
				entityManager.close();
			}
		}

		else {
			logger.error("Unable to write <null> cv to the database");
			throw new NullPointerException("Unable to write <null> cv to the database");
		}
	}
}
