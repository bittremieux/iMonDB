package inspector.jmondb.io;

import com.google.common.collect.Maps;
import inspector.jmondb.model.CV;
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
	 * Persist the given {@link Run} to the database.
	 *
	 * If a Run with the same name was already present, the previous Run is replaced by the given Run.
	 *
	 * @param run  The Run that will be persisted to the database
	 */
	public synchronized void writeRun(Run run) {
		if(run != null) {
			logger.info("Store run <{}>", run.getName());

			EntityManager entityManager = createEntityManager();

			// persist the run in a transaction
			try {
				// check if the run is already in the database and if so, delete the old information
				// (the delete will cascade to the child Values)
				TypedQuery<Long> runQuery = entityManager.createQuery("SELECT run.id FROM Run run WHERE run.name = :name", Long.class);
				runQuery.setParameter("name", run.getName());
				runQuery.setMaxResults(1);	// restrict to a single result (label is unique anyway)

				List<Long> result = runQuery.getResultList();
				if(result.size() > 0) {
					// delete the old run
					logger.info("Duplicate run <name={}>: delete old run <id={}>", run.getName(), result.get(0));
					Run oldRun = entityManager.find(Run.class, result.get(0));
					entityManager.getTransaction().begin();
					entityManager.remove(oldRun);
					entityManager.getTransaction().commit();
				}

				// make sure the existing cv's are retained
				replaceDuplicateCV(run, entityManager);

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
		Map<String, CV> cvLabelMap = Maps.uniqueIndex(cvQuery.getResultList(), CV::getLabel);
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
	 * Persist the given {@link CV} to the database.
	 *
	 * If a CV with the same label was already present in the database, its data will be updated to the given CV.
	 *
	 * @param cv  The cv that will be persisted to the database
	 */
	public synchronized void writeCv(CV cv) {
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
