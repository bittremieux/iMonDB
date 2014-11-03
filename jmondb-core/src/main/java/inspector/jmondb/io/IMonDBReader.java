package inspector.jmondb.io;

import inspector.jmondb.model.*;
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
			logger.warn("Error while creating the EntityManager to connect to the database: {}", e);
			throw new IllegalStateException("Couldn't connect to the database: " + e);
		}
	}

	/**
	 * Retrieves the {@link Instrument} specified by the given name from the database.
	 *
	 * The {@link Event}s that occurred for this {@code Instrument} nor the {@link Run}s that were performed on the {@code Instrument} are not automatically retrieved.
	 *
	 * @param name  the name of the requested {@code Instrument}
	 * @return the {@code Instrument} specified by the given name if it is present in the database, else {@code null}
	 */
	public Instrument getInstrument(String name) {
		return getInstrument(name, false, false);
	}

	/**
	 * Retrieves the {@link Instrument} specified by the given name from the database.
	 *
	 * Unless explicitly specified, the {@link Event}s that occurred on the {@code Instrument} are not retrieved from the database (lazy loading).
	 * The {@link Run}s that were performed on the {@code Instrument} are never automatically retrieved.
	 *
	 * @param name  the name of the requested {@code Instrument}
	 * @param includeEvents  flag that indicates whether the {@code Event}s that occurred on the {@code Instrument} need to be retrieved as well
	 * @param includeProperties  flag that indicates whether the {@code Property}s that are assigned to the {@code Instrument} need to be retrieved as well
	 * @return the {@code Instrument} specified by the given name if it is present in the database, else {@code null}
	 */
	public Instrument getInstrument(String name, boolean includeEvents, boolean includeProperties) {
		logger.debug("Retrieve instrument <{}>", name);

		EntityManager entityManager = createEntityManager();

		try {
			TypedQuery<Instrument> query = entityManager.createQuery("SELECT inst FROM Instrument inst WHERE inst.name = :name", Instrument.class);
			query.setParameter("name", name);

			// get the instrument
			Instrument instrument = query.getSingleResult();
			logger.debug("Instrument <{}> retrieved from the database", name);

			// explicitly load all events if requested (lazy loading)
			if(includeEvents) {
				logger.debug("Load all events for instrument <{}>", name);
				for(Iterator<Event> it = instrument.getEventIterator(); it.hasNext(); ) {
					Event event = it.next();
					logger.trace("Event <{}> retrieved", event.getDate());
				}
			}
			// explicitly load all properties if requested (lazy loading)
			if(includeProperties) {
				logger.debug("Load all properties assigned to instrument <{}>", name);
				for(Iterator<Property> it = instrument.getPropertyIterator(); it.hasNext(); ) {
					Property property = it.next();
					logger.trace("Property <{}> retrieved", property.getName());
				}
			}

			return instrument;
		}
		catch(NoResultException e) {
			logger.debug("Instrument <{}> not found in the database", name);
			return null;
		}
		finally {
			entityManager.close();
		}
	}

	/**
	 * Retrieves the {@link Run} specified by the given name and performed on the {@link Instrument} with the given name from the database.
	 *
	 * @param runName  the name of the requested {@code Run}
	 * @param instrumentName  the name of the {@code Instrument} on which the {@code Run} was performed
	 * @return the {@code Run} specified by the given name and performed on the given {@code Instrument} if present in the database, else {@code null}
	 */
	public Run getRun(String runName, String instrumentName) {
		logger.debug("Retrieve run <{}> for instrument <{}>", runName, instrumentName);

		EntityManager entityManager = createEntityManager();

		try {
			TypedQuery<Run> query = entityManager.createQuery("SELECT run FROM Run run WHERE run.name = :runName AND run.instrument.name = :instName", Run.class);
			query.setParameter("runName", runName);
			query.setParameter("instName", instrumentName);

			// get the run
			Run run = query.getSingleResult();
			logger.debug("Run <{}> retrieved from the database", runName);

			// explicitly load all values and associated properties (lazy loading)
			logger.debug("Load all values and associated properties from run <{}>", runName);
			for(Iterator<Value> valIt = run.getValueIterator(); valIt.hasNext(); )
				logger.trace("Value and property <{}> retrieved", valIt.next().getDefiningProperty().getAccession());

			// explicitly load the run's instrument
			logger.debug("Load the instrument on which run <{}> was performed", runName);
			run.getInstrument().hashCode();

			return run;
		}
		catch(NoResultException e) {
			logger.debug("Run <{}> not found for instrument <{}> in the database", runName, instrumentName);
			return null;
		}
		finally {
			entityManager.close();
		}
	}

	/**
	 * Retrieves the {@link Property} specified by the given accession.
	 *
	 * @param accession  the accession of the requested {@code Property}
	 * @return the {@code Property} specified by the given accession if found, else {@code null}
	 */
	public Property getProperty(String accession) {
		logger.debug("Retrieve property <{}>", accession);

		EntityManager entityManager = createEntityManager();

		try {
			TypedQuery<Property> query = entityManager.createQuery("SELECT prop FROM Property prop WHERE prop.accession = :accession", Property.class);
			query.setParameter("accession", accession);

			// get the property
			Property property = query.getSingleResult();
			logger.debug("Property <{}> retrieved from the database", accession);

			// explicitly retrieve all values and associated runs for the property
			logger.debug("Load all values and associated runs for property <{}>", accession);
			for(Iterator<Value> valIt = property.getValueIterator(); valIt.hasNext(); ) {
				Run run = valIt.next().getOriginatingRun();
				run.getInstrument().hashCode();
				logger.trace("Value and run <{}> retrieved", run.getName());
			}

			return property;
		}
		catch(NoResultException e) {
			logger.debug("Property <{}> not found in the database", accession);
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
			logger.debug("Execute custom query: {}", queryStr);

			EntityManager entityManager = createEntityManager();

			try {
				TypedQuery<T> query = entityManager.createQuery(queryStr, clss);

				if(parameters != null)
					for(Map.Entry<String, String> entry : parameters.entrySet()) {
						logger.trace("Set parameter <{}>: <{}>", entry.getKey(), entry.getValue());
						query.setParameter(entry.getKey(), entry.getValue());
					}

				List<T> result = query.getResultList();
				logger.trace("Result list retrieved from the database");

				return result;
			}
			finally {
				entityManager.close();
			}
		}
		else {
			logger.debug("Unable to execute <null> query");
			return null;
		}
	}
}
