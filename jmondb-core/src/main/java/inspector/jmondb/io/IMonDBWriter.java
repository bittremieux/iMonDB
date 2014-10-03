package inspector.jmondb.io;

import com.google.common.collect.Maps;
import inspector.jmondb.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.util.*;

/**
 * An iMonDB output writer to write to an RDBMS.
 */
public class IMonDBWriter {

	private static final Logger logger = LogManager.getLogger(IMonDBWriter.class);

	/** {@link EntityManagerFactory} used to set up connections to the database */
	private EntityManagerFactory emf;

	/**
	 * Creates an {@code IMonDBWriter} specified by the given {@link EntityManagerFactory}.
	 *
	 * @param emf  the {@code EntityManagerFactory} used to set up the connection to the database, not {@code null}
	 */
	public IMonDBWriter(EntityManagerFactory emf) {
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
	 * Write the given {@link Instrument} to the database.
	 *
	 * If an {@code Instrument} with the same name was already present in the database, an {@link IllegalArgumentException} will be thrown.
	 *
	 * The {@link Run}s performed on the given {@code Instrument} will <em>not</em> be written to the database.
	 * The {@link CV} used to define the {@code Instrument} on the other hand will be written to the database or updated if it is already present.
	 *
	 * @param instrument  the {@code Instrument} that will be written to the database, not {@code null}
	 */
	public void writeInstrument(Instrument instrument) {
		if(instrument != null) {
			logger.info("Store instrument <{}>", instrument.getName());

			EntityManager entityManager = createEntityManager();

			try {
				// cancel if the instrument is already in the database
				TypedQuery<Long> query = entityManager.createQuery("SELECT inst.id FROM Instrument inst WHERE inst.name = :name", Long.class);
				query.setParameter("name", instrument.getName());
				query.setMaxResults(1);	// restrict to a single result
				List<Long> result = query.getResultList();
				if(result.size() > 0) {
					logger.error("Instrument <{}> already exists with id <{}>", instrument.getName(), result.get(0));
					throw new IllegalArgumentException("Instrument <" + instrument.getName() + " already exists with id <" + result.get(0) + ">");
				}

				// make sure a pre-existing cv is updated
				assignDuplicateCvId(instrument.getCv(), entityManager);

				// store this instrument
				entityManager.getTransaction().begin();
				entityManager.merge(instrument);
				entityManager.getTransaction().commit();
			}
			catch(EntityExistsException e) {
				try {
					logger.debug("Rollback because instrument <{}> already exists in the database: {}", instrument.getName(), e.getMessage());
					entityManager.getTransaction().rollback();
				}
				catch(PersistenceException p) {
					logger.debug("Unable to rollback for instrument <{}>: {}", instrument.getName(), p.getMessage());
				}

				logger.error("Unable to store instrument <{}>: {}", instrument.getName(), e.getMessage());
				throw new IllegalArgumentException("Unable to store instrument <" + instrument.getName() + ">");
			}
			catch(RollbackException e) {
				logger.error("Unable to store instrument <{}>: {}", instrument.getName(), e.getMessage());
				throw new IllegalArgumentException("Unable to store instrument <" + instrument.getName() + ">");
			}
			finally {
				entityManager.close();
			}
		}
		else {
			logger.error("Unable to store <null> instrument");
			throw new NullPointerException("Unable to persist <null> instrument");
		}
	}

	/**
	 * Make sure a duplicate {@link CV} is not persisted multiple times to the database.
	 *
	 * If the {@code CV} is already present in the database, assign its id to the new {@code CV}, so the original {@code CV} (and its relationships) will be retained (but updated information will be overwritten).
	 *
	 * @param cv  the {@code CV} that will be checked, not {@code null}
	 * @param entityManager  the connection to the database, not {@code null}
	 */
	private void assignDuplicateCvId(CV cv, EntityManager entityManager) {
		logger.debug("Checking if cv <{}> is already present in the database", cv.getLabel());

		// check if the cv already exists in the database
		TypedQuery<Long> cvQuery = entityManager.createQuery("SELECT cv.id FROM CV cv WHERE cv.label = :label", Long.class);
		cvQuery.setParameter("label", cv.getLabel());
		cvQuery.setMaxResults(1);	// restrict to a single result

		// if so, assign its id to the new cv
		List<Long> result = cvQuery.getResultList();
		if(result.size() > 0) {
			logger.debug("Duplicate cv <{}>: assign id <{}>", cv.getLabel(), result.get(0));
			cv.setId(result.get(0));
		}
	}

	/**
	 * Write the given {@link Event} to the database.
	 *
	 * If an {@code Event} which occurred on the same {@link Instrument} at the same time was already present, the previous {@code Event} is updated to the given {@code Event}.
	 *
	 * If the {@link Instrument} for which the {@code Event} occurred is not present in the database, an {@link IllegalStateException} will be thrown.
	 *
	 * @param event  the {@code Event} that will be written to the database, not {@code null}
	 */
	public void writeOrUpdateEvent(Event event) {
		if(event != null) {
			logger.info("Store event <{}> which occurred on instrument <{}>", event.getDate(), event.getInstrument().getName());

			EntityManager entityManager = createEntityManager();

			try {
				// cancel if the instrument for which the event occurred is not yet in the database
				TypedQuery<Long> instQuery = entityManager.createQuery("SELECT inst.id FROM Instrument inst WHERE inst.name = :name", Long.class);
				instQuery.setParameter("name", event.getInstrument().getName());
				instQuery.setMaxResults(1);	// restrict to a single result
				List<Long> instResult = instQuery.getResultList();
				if(instResult.size() == 0) {
					logger.error("Instrument <{}> for which event <{}> occurred is not in the database yet", event.getInstrument().getName(), event.getDate());
					throw new IllegalStateException("Instrument <" + event.getInstrument().getName() + "> for which event <" + event.getDate() + "> occurred is not in the database yet");
				}
				// else, assign the correct id for the instrument
				else {
					logger.debug("Existing instrument <{}>: assign id <{}>", event.getInstrument().getName(), instResult.get(0));
					event.getInstrument().setId(instResult.get(0));
				}

				// check if the event is already in the database and assign its id
				TypedQuery<Long> eventQuery = entityManager.createQuery("SELECT event.id FROM Event event WHERE event.date = :date AND event.instrument.name = :instName", Long.class);
				eventQuery.setParameter("date", event.getDate());
				eventQuery.setParameter("instName", event.getInstrument().getName());
				eventQuery.setMaxResults(1);	// restrict to a single result
				List<Long> eventResult = eventQuery.getResultList();
				if(eventResult.size() > 0) {
					logger.debug("Existing event <{}> which occurred on instrument <{}>: assign id <{}>", event.getDate(),  event.getInstrument().getName(), eventResult.get(0));
					event.setId(eventResult.get(0));
				}

				// store the new event
				entityManager.getTransaction().begin();
				entityManager.merge(event);
				entityManager.getTransaction().commit();
			}
			catch(EntityExistsException e) {
				try {
					logger.debug("Rollback because event <{}> already exists in the database: {}", event.getDate(), e.getMessage());
					entityManager.getTransaction().rollback();
				}
				catch(PersistenceException p) {
					logger.debug("Unable to rollback for event <{}>: {}", event.getDate(), p.getMessage());
				}

				logger.error("Unable to store event <{}>: {}", event.getDate(), e.getMessage());
				throw new IllegalArgumentException("Unable to store event <" + event.getDate() + ">");
			}
			catch(RollbackException e) {
				logger.error("Unable to store event <{}>: {}", event.getDate(), e.getMessage());
				throw new IllegalArgumentException("Unable to store event <" + event.getDate() + ">");
			}
			finally {
				entityManager.close();
			}
		}
		else {
			logger.error("Unable to store <null> event");
			throw new NullPointerException("Unable to persist <null> event");
		}
	}

	/**
	 * Write the given {@link Run} to the database.
	 *
	 * If the {@link Instrument} on which the {@code Run} is performed is not present in the database, an {@link IllegalStateException} will be thrown.
	 * If a {@code Run} with the same name performed on the same {@code Instrument} was already present in the database, an {@link IllegalArgumentException} will be thrown.
	 *
	 * All child {@link Value}s and their associated {@link Property}s and {@link CV}'s will be written to the database as well.
	 * If some of these {@code Property}s or {@code CV}'s were already present in the database, they will be updated.
	 *
	 * @param run  the {@code Run} that will be written to the database, not {@code null}
	 */
	public void writeRun(Run run) {
		if(run != null) {
			logger.info("Store run <{}> for instrument <{}>", run.getName(), run.getInstrument().getName());

			EntityManager entityManager = createEntityManager();

			try {
				// cancel if the run's instrument is not yet in the database
				TypedQuery<Long> instQuery = entityManager.createQuery("SELECT inst.id FROM Instrument inst WHERE inst.name = :name", Long.class);
				instQuery.setParameter("name", run.getInstrument().getName());
				instQuery.setMaxResults(1);	// restrict to a single result
				List<Long> instResult = instQuery.getResultList();
				if(instResult.size() == 0) {
					logger.error("Instrument <{}> for run <{}> is not in the database yet", run.getInstrument().getName(), run.getName());
					throw new IllegalStateException("Instrument <" + run.getInstrument().getName() + "> for run <" + run.getName() + "> is not in the database yet");
				}
				// else, assign the correct id for the instrument and its referenced cv
				else {
					logger.debug("Existing instrument <{}>: assign id <{}>", run.getInstrument().getName(), instResult.get(0));
					run.getInstrument().setId(instResult.get(0));
					assignDuplicateCvId(run.getInstrument().getCv(), entityManager);
				}

				// cancel if the run is already in the database
				TypedQuery<Long> runQuery = entityManager.createQuery("SELECT run.id FROM Run run WHERE run.name = :name AND run.instrument.name = :instName", Long.class);
				runQuery.setParameter("name", run.getName());
				runQuery.setParameter("instName", run.getInstrument().getName());
				runQuery.setMaxResults(1);	// restrict to a single result
				List<Long> runResult = runQuery.getResultList();
				if(runResult.size() > 0) {
					logger.error("Run <{}> for instrument <{}> already exists with id <{}>", run.getName(),  run.getInstrument().getName(), runResult.get(0));
					throw new IllegalArgumentException("Run <" + run.getName() + "> for instrument <" + run.getInstrument().getName() + " already exists with id <" + runResult.get(0) + ">");
				}

				// make sure the pre-existing properties and corresponding cv's are retained
				HashMap<String, Property> properties = new HashMap<>();
				for(Iterator<Value> it = run.getValueIterator(); it.hasNext(); ) {
					Property prop = it.next().getDefiningProperty();
					properties.put(prop.getAccession(), prop);
				}
				assignDuplicatePropertyCvId(properties, entityManager);

				// store the new run
				entityManager.getTransaction().begin();
				entityManager.merge(run);
				entityManager.getTransaction().commit();
			}
			catch(EntityExistsException e) {
				try {
					logger.debug("Rollback because run <{}> already exists in the database: {}", run.getName(), e.getMessage());
					entityManager.getTransaction().rollback();
				}
				catch(PersistenceException p) {
					logger.debug("Unable to rollback for run <{}>: {}", run.getName(), p.getMessage());
				}

				logger.error("Unable to store run <{}>: {}", run.getName(), e.getMessage());
				throw new IllegalArgumentException("Unable to store run <" + run.getName() + ">");
			}
			catch(RollbackException e) {
				logger.error("Unable to store run <{}>: {}", run.getName(), e.getMessage());
				throw new IllegalArgumentException("Unable to store run <" + run.getName() + ">");
			}
			finally {
				entityManager.close();
			}
		}
		else {
			logger.error("Unable to store <null> run");
			throw new NullPointerException("Unable to persist <null> run");
		}
	}

	/**
	 * Make sure duplicate {@link Property}s and {@link CV}s are not persisted multiple times to the database.
	 *
	 * If an item is already present in the database, assign its id to the new item, so the original item (and its relationships) will be retained (but updated information will be overwritten).
	 *
	 * @param properties  a {@code Map} with {@code Property}s as values and their {@code accession} as keys, not {@code null}
	 * @param entityManager  the connection to the database, not {@code null}
	 */
	private void assignDuplicatePropertyCvId(Map<String, Property> properties, EntityManager entityManager) {
		logger.debug("Updating all properties in the database associated to the run");

		// get all pre-existing properties that have the same accession number
		TypedQuery<IdDataPair> propQuery = entityManager.createQuery("SELECT NEW inspector.jmondb.io.IdDataPair(prop.id, prop.accession) FROM Property prop WHERE prop.accession in :propAccessions", IdDataPair.class);
		propQuery.setParameter("propAccessions", properties.keySet());
		Map<String, Long> propAccessionIdMap = new HashMap<>();
		for(IdDataPair propPair : propQuery.getResultList())
			propAccessionIdMap.put((String) propPair.getData(), propPair.getId());

		// get all pre-existing cv's (not filtered on label, but should be a low number of items)
		TypedQuery<IdDataPair> cvQuery = entityManager.createQuery("SELECT NEW inspector.jmondb.io.IdDataPair(cv.id, cv.label) FROM CV cv", IdDataPair.class);
		Map<String, Long> cvLabelIdMap = new HashMap<>();
		for(IdDataPair cvPair : cvQuery.getResultList())
			cvLabelIdMap.put((String) cvPair.getData(), cvPair.getId());

		// assign id's from pre-existing entities
		for(Property prop : properties.values()) {
			if(prop.getId() == null && propAccessionIdMap.containsKey(prop.getAccession())) {
				prop.setId(propAccessionIdMap.get(prop.getAccession()));
				logger.debug("Duplicate property <{}>: assign id <{}>", prop.getAccession(), prop.getId());
			}
			CV cv = prop.getCv();
			if(cv.getId() == null && cvLabelIdMap.containsKey(cv.getLabel())) {
				cv.setId(cvLabelIdMap.get(cv.getLabel()));
				logger.debug("Duplicate cv <label={}>: assign id <{}>", cv.getLabel(), cv.getId());
			}
		}
	}

	/**
	 * Write the given {@link Property} to the database.
	 *
	 * If a {@code Property} with the same {@link CV} and accession combination was already present in the database, it will be updated to the given {@code Property}.
	 *
	 * The child {@link Value}s that are defined by the given {@code Property} will <em>not</em> be written to the database.
	 * The referenced {@code CV} on the other hand will be written to the database or updated if it was already present.
	 *
	 * @param property  the {@code Property} that will be written to the database, not {@code null}
	 */
	public synchronized void writeProperty(Property property) {
		if(property != null) {
			logger.info("Store property <{}>", property.getAccession());

			EntityManager entityManager = createEntityManager();

			// persist the Property in a transaction
			try {
				// check if the Property is already in the database and retrieve its primary key
				TypedQuery<Long> query = entityManager.createQuery("SELECT prop.id FROM Property prop WHERE prop.accession = :accession AND prop.cv = :cv", Long.class);
				query.setParameter("accession", property.getAccession());
				query.setParameter("cv", property.getCv()).setMaxResults(1);
				List<Long> result = query.getResultList();
				if(result.size() > 0) {
					logger.info("Duplicate property <cv={}#{}>: assign id <{}>", property.getCv().getLabel(), property.getAccession(), result.get(0));
					property.setId(result.get(0));
				}

				// make sure the pre-existing cv's are retained
				//assignDuplicatePropertyCvId(Arrays.asList(property), entityManager);

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
					logger.error("Unable to rollback the transaction for property <{}> to the database: {}", property.getAccession(), p);
				}

				logger.error("Unable to persist property <{}> to the database: {}", property.getAccession(), e);
				throw new IllegalArgumentException("Unable to persist property <" + property.getAccession() + "> to the database");
			}
			catch(RollbackException e) {
				logger.error("Unable to commit the transaction for property <{}> to the database: {}", property.getAccession(), e);
				throw new IllegalArgumentException("Unable to commit the transaction for property <" + property.getAccession() + "> to the database");
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

	/**
	 * Write the given {@link CV} to the database.
	 *
	 * If a {@code CV} with the same label was already present in the database, it will be updated to the given {@code CV}.
	 *
	 * @param cv  the {@code CV} that will be written to the database, not {@code null}
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

				// store this CV
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
