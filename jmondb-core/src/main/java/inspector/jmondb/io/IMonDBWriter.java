package inspector.jmondb.io;

import com.google.common.collect.Maps;
import inspector.jmondb.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.sql.Timestamp;
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
	 * If an {@code Instrument} with the same name was already present in the database, it will be updated to the given {@code Instrument}.
	 *
	 * The {@link Run}s performed on the given {@code Instrument} will <em>not</em> be written to the database.
	 * The {@link CV} used to define the {@code Instrument} on the other hand will be written to the database or updated if it is already present.
	 * The {@link Event}s that occurred on the given {@code Instrument} will be written to the database.
	 *
	 * @param instrument  the {@code Instrument} that will be written to the database, not {@code null}
	 */
	public synchronized void writeInstrument(Instrument instrument) {
		if(instrument != null) {
			logger.info("Store instrument <{}>", instrument.getName());

			EntityManager entityManager = createEntityManager();

			// persist the instrument in a transaction
			try {
				// check if the instrument is already in the database and retrieve its primary key
				TypedQuery<Long> query = entityManager.createQuery("SELECT inst.id FROM Instrument inst WHERE inst.name = :name", Long.class);
				query.setParameter("name", instrument.getName());
				query.setMaxResults(1);	// restrict to a single result
				List<Long> result = query.getResultList();
				if(result.size() > 0) {
					logger.debug("Duplicate instrument <{}>: assign id <{}>", instrument.getName(), result.get(0));
					instrument.setId(result.get(0));

					// make sure pre-existing events are updated
					ArrayList<Event> events = new ArrayList<>();
					for(Iterator<Event> it = instrument.getEventIterator(); it.hasNext(); )
						events.add(it.next());
					assignDuplicateEventId(instrument.getId(), events, entityManager);
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
					logger.error("Unable to rollback for instrument <{}>: {}", instrument.getName(), p.getMessage());
				}

				logger.error("Unable to persist instrument <{}>: {}", instrument.getName(), e.getMessage());
				throw new IllegalArgumentException("Unable to persist instrument <" + instrument.getName() + ">");
			}
			catch(RollbackException e) {
				logger.error("Unable to persist instrument <{}>: {}", instrument.getName(), e.getMessage());
				throw new IllegalArgumentException("Unable to persist instrument <" + instrument.getName() + ">");
			}
			finally {
				entityManager.close();
			}
		}
		else {
			logger.error("Unable to persist <null> instrument");
			throw new NullPointerException("Unable to persist <null> instrument");
		}
	}

	/**
	 * Assign the correct {@code id} to pre-existing {@link Event}s in the database.
	 *
	 * This ensures that pre-existing {@code Event}s are correctly merged, (possibly) updating the old event information.
	 *
	 * For a specific {@link Instrument} all {@code Event}s have a unique event date.
	 *
	 * @param instrumentId  the {@code id} of the {@code Instrument} on which the events occurred, not {@code null}
	 * @param events  a list of {@code Event}s, not {@code null}
	 * @param entityManager  the connection to the database, not {@code null}
	 */
	private void assignDuplicateEventId(Long instrumentId, List<Event> events, EntityManager entityManager) {
		logger.debug("Checking for pre-existing events for instrument <{}>", instrumentId);

		// get all pre-existing events for the given instrument from the database
		TypedQuery<IdDataPair> eventQuery = entityManager.createQuery("SELECT NEW inspector.jmondb.io.IdDataPair(event.id, event.date) FROM Event event WHERE event.instrument.id = :instrumentId", IdDataPair.class);
		eventQuery.setParameter("instrumentId", instrumentId);
		HashMap<Timestamp, Long> eventDateIdMap = new HashMap<>();
		for(IdDataPair mapping : eventQuery.getResultList())
			eventDateIdMap.put((Timestamp)mapping.getData(), mapping.getId());

		// assign id's from pre-existing events
		for(Event event : events)
			if(eventDateIdMap.containsKey(event.getDate())) {
				event.setId(eventDateIdMap.get(event.getDate()));
				logger.debug("Duplicate event <{}>: assign id <{}>", event.getDate(), event.getId());
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
	 * Write the given {@link Run} to the database.
	 *
	 * If a {@code Run} with the same name was already present, the previous {@code Run} is replaced by the given {@code Run}.
	 *
	 * All child {@link Value}s and their associated {@link Property}s and {@link CV}'s will be written to the database as well.
	 * If some of these {@code Property}s or {@code CV}'s were already present in the database, they will be updated.
	 *
	 * @param run  the {@code Run} that will be written to the database, not {@code null}
	 */
	public synchronized void writeRun(Run run) {
		if(run != null) {
			logger.info("Store run <{}>", run.getName());

			EntityManager entityManager = createEntityManager();

			// persist the run in a transaction
			try {
				// check if the run is already in the database and if so, delete the old run
				// (the delete will cascade to the child values)
				TypedQuery<Long> runQuery = entityManager.createQuery("SELECT run.id FROM Run run WHERE run.name = :name", Long.class);
				runQuery.setParameter("name", run.getName());
				runQuery.setMaxResults(1);	// restrict to a single result (name is unique anyway)

				List<Long> result = runQuery.getResultList();
				if(result.size() > 0) {
					// delete the old run (delete will be cascaded to the values)
					logger.info("Duplicate run <name={}>: delete old run <id={}>", run.getName(), result.get(0));
					Run oldRun = entityManager.find(Run.class, result.get(0));
					entityManager.getTransaction().begin();
					entityManager.remove(oldRun);
					entityManager.getTransaction().commit();
				}

				// make sure the pre-existing instruments are retained
				assignDuplicateInstrumentId(run.getInstrument(), entityManager);
				// make sure the pre-existing properties and cv's are retained
				ArrayList<Property> properties = new ArrayList<>();
				for(Iterator<Value> it = run.getValueIterator(); it.hasNext(); )
					properties.add(it.next().getDefiningProperty());
				assignDuplicatePropertyCvId(properties, entityManager);

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

				logger.error("Unable to write run <{}> to the database: {}", run.getName(), e);
				throw new IllegalArgumentException("Unable to write run <" + run.getName() + "> to the database");
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
	 * Make sure a duplicate {@link Instrument} is not persisted multiple times to the database.
	 *
	 * If the {@code Instrument} is already present in the database, assign its id to the new {@code Instrument}, so the original {@code Instrument} (and its relationships) will be retained (but updated information will be overwritten).
	 *
	 * @param instrument  the {@code Instrument} that will be checked, not {@code null}
	 * @param entityManager  the connection to the database, not {@code null}
	 */
	private void assignDuplicateInstrumentId(Instrument instrument, EntityManager entityManager) {
		TypedQuery<Long> instrumentQuery = entityManager.createQuery("SELECT inst.id FROM Instrument inst WHERE inst.name = :name AND inst.type = :type", Long.class);
		instrumentQuery.setParameter("name", instrument.getName());
		instrumentQuery.setParameter("type", instrument.getType());
		instrumentQuery.setMaxResults(1);	// restrict to a single result

		List<Long> result = instrumentQuery.getResultList();
		if(result.size() > 0)
			instrument.setId(result.get(0));
	}

	/**
	 * Make sure duplicate {@link Property}s and {@link CV}'s are not persisted multiple times to the database.
	 *
	 * If an item is already present in the database, assign its id to the new item, so the original item (and its relationships) will be retained (but updated information will be overwritten).
	 *
	 * @param properties  the {@code List} with {@code Property}s that will be checked, not {@code null}
	 * @param entityManager  the connection to the database, not {@code null}
	 */
	private void assignDuplicatePropertyCvId(List<Property> properties, EntityManager entityManager) {
		// get all pre-existing properties and cv's from the database
		ArrayList<String> propAccessions = new ArrayList<>();
		ArrayList<String> cvLabels = new ArrayList<>();
		for(Property prop : properties) {
			propAccessions.add(prop.getAccession());
			cvLabels.add(prop.getCv().getLabel());
		}

		TypedQuery<Property> propQuery = entityManager.createQuery("SELECT prop FROM Property prop WHERE prop.accession in :propAccessions", Property.class);
		propQuery.setParameter("propAccessions", propAccessions);
		Map<String, Property> propAccessionMap = Maps.uniqueIndex(propQuery.getResultList(), Property::getAccession);

		TypedQuery<CV> cvQuery = entityManager.createQuery("SELECT cv FROM CV cv WHERE cv.label in :cvLabels", CV.class);
		cvQuery.setParameter("cvLabels", cvLabels);
		Map<String, CV> cvLabelMap = Maps.uniqueIndex(cvQuery.getResultList(), CV::getLabel);

		// assign id's from pre-existing items to new items
		for(Property prop : properties) {
			if(prop.getId() == null && propAccessionMap.containsKey(prop.getAccession())) {
				prop.setId(propAccessionMap.get(prop.getAccession()).getId());
				logger.info("Duplicate Property <accession={}>: assign id <{}>", prop.getAccession(), prop.getId());
			}
			CV cv = prop.getCv();
			if(cv.getId() == null && cvLabelMap.containsKey(cv.getLabel())) {
				cv.setId(cvLabelMap.get(cv.getLabel()).getId());
				logger.info("Duplicate CV <label={}>: assign id <{}>", cv.getLabel(), cv.getId());
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
				assignDuplicatePropertyCvId(Arrays.asList(property), entityManager);

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
