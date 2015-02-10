package inspector.imondb.io;

/*
 * #%L
 * iMonDB Core
 * %%
 * Copyright (C) 2014 - 2015 InSPECtor
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import inspector.imondb.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * An iMonDB output writer to write to an RDBMS.
 *
 * <em>Attention:</em> This class is not thread-safe!
 * If multiple threads try to write to the same database concurrently, this must be synchronized externally.
 * (For example: most {@link Instrument}s will refer to the same {@link CV}, {@link Property}s are applicable for multiple {@link Value}s, ...)
 */
public class IMonDBWriter {

    private static final Logger LOGGER = LogManager.getLogger(IMonDBWriter.class);

    /** {@link EntityManagerFactory} used to set up connections to the database */
    private EntityManagerFactory emf;

    /**
     * Creates an {@code IMonDBWriter} specified by the given {@link EntityManagerFactory}.
     *
     * @param emf  the {@code EntityManagerFactory} used to set up the connection to the database, not {@code null}
     */
    public IMonDBWriter(EntityManagerFactory emf) {
        if(emf != null) {
            this.emf = emf;
        } else {
            LOGGER.error("The EntityManagerFactory is not allowed to be <null>");
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
        } catch(Exception e) {
            LOGGER.error("Error while creating the EntityManager to connect to the database: {}", e.getMessage());
            throw new IllegalStateException("Couldn't connect to the database: " + e.getMessage(), e);
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
            LOGGER.debug("Store instrument <{}>", instrument.getName());

            EntityManager entityManager = createEntityManager();

            try {
                // cancel if the instrument is already in the database
                TypedQuery<Long> query = entityManager.createQuery("SELECT inst.id FROM Instrument inst WHERE inst.name = :name", Long.class);
                query.setParameter("name", instrument.getName());
                query.setMaxResults(1);    // restrict to a single result
                List<Long> result = query.getResultList();
                if(!result.isEmpty()) {
                    LOGGER.error("Instrument <{}> already exists with id <{}>", instrument.getName(), result.get(0));
                    throw new IllegalArgumentException("Instrument <" + instrument.getName() + " already exists with id <" + result.get(0) + ">");
                }

                // make sure a pre-existing cv is updated
                assignDuplicateCvId(instrument.getCv(), entityManager);
                // make sure the pre-existing properties and corresponding cv's are retained
                Map<String, Property> properties = new HashMap<>();
                for(Iterator<Property> it = instrument.getPropertyIterator(); it.hasNext(); ) {
                    Property prop = it.next();
                    properties.put(prop.getAccession(), prop);
                }
                if(!properties.isEmpty()) {
                    assignDuplicatePropertyCvId(properties, entityManager);
                }

                // store this instrument
                entityManager.getTransaction().begin();
                entityManager.merge(instrument);
                entityManager.getTransaction().commit();
            } catch(EntityExistsException e) {
                LOGGER.error("Unable to store instrument <{}>: {}", instrument.getName(), e.getMessage());

                try {
                    LOGGER.debug("Rollback because instrument <{}> already exists in the database: {}", instrument.getName(), e.getMessage());
                    entityManager.getTransaction().rollback();
                } catch(PersistenceException p) {
                    LOGGER.debug("Unable to rollback for instrument <{}>: {}", instrument.getName(), p.getMessage(), p);
                }

                throw new IllegalArgumentException("Unable to store instrument <" + instrument.getName() + ">", e);
            } catch(RollbackException e) {
                LOGGER.error("Unable to store instrument <{}>: {}", instrument.getName(), e.getMessage());
                throw new IllegalArgumentException("Unable to store instrument <" + instrument.getName() + ">", e);
            } finally {
                entityManager.close();
            }
        } else {
            LOGGER.error("Unable to store <null> instrument");
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
        LOGGER.debug("Checking if cv <{}> is already present in the database", cv.getLabel());

        // check if the cv already exists in the database
        TypedQuery<Long> cvQuery = entityManager.createQuery("SELECT cv.id FROM CV cv WHERE cv.label = :label", Long.class);
        cvQuery.setParameter("label", cv.getLabel());
        cvQuery.setMaxResults(1);    // restrict to a single result

        // if so, assign its id to the new cv
        List<Long> result = cvQuery.getResultList();
        if(!result.isEmpty()) {
            LOGGER.trace("Duplicate cv <{}>: assign id <{}>", cv.getLabel(), result.get(0));
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
            LOGGER.debug("Store event <{}> which occurred on instrument <{}>", event.getDate(), event.getInstrument().getName());

            EntityManager entityManager = createEntityManager();

            try {
                // cancel if the instrument for which the event occurred is not yet in the database
                TypedQuery<Long> instQuery = entityManager.createQuery("SELECT inst.id FROM Instrument inst WHERE inst.name = :name", Long.class);
                instQuery.setParameter("name", event.getInstrument().getName());
                instQuery.setMaxResults(1);    // restrict to a single result
                List<Long> instResult = instQuery.getResultList();
                if(instResult.isEmpty()) {
                    LOGGER.error("Instrument <{}> for which event <{}> occurred is not in the database yet", event.getInstrument().getName(), event.getDate());
                    throw new IllegalStateException("Instrument <" + event.getInstrument().getName() + "> for which event <" + event.getDate() + "> occurred is not in the database yet");
                } else {
                    // else, assign the correct id for the instrument
                    LOGGER.trace("Existing instrument <{}>: assign id <{}>", event.getInstrument().getName(), instResult.get(0));
                    event.getInstrument().setId(instResult.get(0));
                }

                // check if the event is already in the database and assign its id
                TypedQuery<Long> eventQuery = entityManager.createQuery("SELECT event.id FROM Event event WHERE event.date = :date AND event.instrument.name = :instName", Long.class);
                eventQuery.setParameter("date", event.getDate());
                eventQuery.setParameter("instName", event.getInstrument().getName());
                eventQuery.setMaxResults(1);    // restrict to a single result
                List<Long> eventResult = eventQuery.getResultList();
                if(!eventResult.isEmpty()) {
                    LOGGER.trace("Existing event <{}> which occurred on instrument <{}>: assign id <{}>", event.getDate(), event.getInstrument().getName(), eventResult.get(0));
                    event.setId(eventResult.get(0));
                }

                // store the new event
                entityManager.getTransaction().begin();
                entityManager.merge(event);
                entityManager.getTransaction().commit();
            } catch(EntityExistsException e) {
                LOGGER.error("Unable to store event <{}>: {}", event.getDate(), e.getMessage());

                try {
                    LOGGER.debug("Rollback because event <{}> already exists in the database: {}", event.getDate(), e.getMessage());
                    entityManager.getTransaction().rollback();
                } catch(PersistenceException p) {
                    LOGGER.debug("Unable to rollback for event <{}>: {}", event.getDate(), p.getMessage(), p);
                }

                throw new IllegalArgumentException("Unable to store event <" + event.getDate() + ">", e);
            } catch(RollbackException e) {
                LOGGER.error("Unable to store event <{}>: {}", event.getDate(), e.getMessage());
                throw new IllegalArgumentException("Unable to store event <" + event.getDate() + ">", e);
            } finally {
                entityManager.close();
            }
        } else {
            LOGGER.error("Unable to store <null> event");
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
            LOGGER.debug("Store run <{}> for instrument <{}>", run.getName(), run.getInstrument().getName());

            EntityManager entityManager = createEntityManager();

            try {
                // cancel if the run's instrument is not yet in the database
                TypedQuery<Long> instQuery = entityManager.createQuery("SELECT inst.id FROM Instrument inst WHERE inst.name = :name", Long.class);
                instQuery.setParameter("name", run.getInstrument().getName());
                instQuery.setMaxResults(1);    // restrict to a single result
                List<Long> instResult = instQuery.getResultList();
                if(instResult.isEmpty()) {
                    LOGGER.error("Instrument <{}> for run <{}> is not in the database yet", run.getInstrument().getName(), run.getName());
                    throw new IllegalStateException("Instrument <" + run.getInstrument().getName() + "> for run <" + run.getName() + "> is not in the database yet");
                } else {
                    // else, assign the correct id for the instrument and its referenced cv
                    LOGGER.trace("Existing instrument <{}>: assign id <{}>", run.getInstrument().getName(), instResult.get(0));
                    run.getInstrument().setId(instResult.get(0));
                    assignDuplicateCvId(run.getInstrument().getCv(), entityManager);
                }

                // cancel if the run is already in the database
                TypedQuery<Long> runQuery = entityManager.createQuery("SELECT run.id FROM Run run WHERE run.name = :name AND run.instrument.name = :instName", Long.class);
                runQuery.setParameter("name", run.getName());
                runQuery.setParameter("instName", run.getInstrument().getName());
                runQuery.setMaxResults(1);    // restrict to a single result
                List<Long> runResult = runQuery.getResultList();
                if(!runResult.isEmpty()) {
                    LOGGER.error("Run <{}> for instrument <{}> already exists with id <{}>", run.getName(), run.getInstrument().getName(), runResult.get(0));
                    throw new IllegalArgumentException("Run <" + run.getName() + "> for instrument <" + run.getInstrument().getName() + "> already exists with id <" + runResult.get(0) + ">");
                }

                // make sure the pre-existing properties and corresponding cv's are retained
                Map<String, Property> properties = new HashMap<>();
                for(Iterator<Property> it = run.getInstrument().getPropertyIterator(); it.hasNext(); ) {
                    Property prop = it.next();
                    properties.put(prop.getAccession(), prop);
                }
                if(!properties.isEmpty()) {
                    assignDuplicatePropertyCvId(properties, entityManager);
                }

                // store the new run
                entityManager.getTransaction().begin();
                entityManager.merge(run);
                entityManager.getTransaction().commit();
            } catch(EntityExistsException e) {
                LOGGER.error("Unable to store run <{}>: {}", run.getName(), e.getMessage());

                try {
                    LOGGER.debug("Rollback because run <{}> already exists in the database: {}", run.getName(), e.getMessage());
                    entityManager.getTransaction().rollback();
                } catch(PersistenceException p) {
                    LOGGER.debug("Unable to rollback for run <{}>: {}", run.getName(), p.getMessage(), p);
                }

                throw new IllegalArgumentException("Unable to store run <" + run.getName() + ">", e);
            } catch(RollbackException e) {
                LOGGER.error("Unable to store run <{}>: {}", run.getName(), e.getMessage());
                throw new IllegalArgumentException("Unable to store run <" + run.getName() + ">", e);
            } finally {
                entityManager.close();
            }
        } else {
            LOGGER.error("Unable to store <null> run");
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
        LOGGER.debug("Updating all properties in the database associated to the run");

        // get all pre-existing properties that have the same accession number
        TypedQuery<IdDataPair> propQuery = entityManager.createQuery("SELECT NEW inspector.imondb.io.IdDataPair(prop.id, prop.accession) FROM Property prop WHERE prop.accession in :propAccessions", IdDataPair.class);
        propQuery.setParameter("propAccessions", properties.keySet());
        Map<String, Long> propAccessionIdMap = new HashMap<>();
        for(IdDataPair propPair : propQuery.getResultList()) {
            propAccessionIdMap.put((String) propPair.getData(), propPair.getId());
        }

        // get all pre-existing cv's (not filtered on label, but should be a low number of items)
        TypedQuery<IdDataPair> cvQuery = entityManager.createQuery("SELECT NEW inspector.imondb.io.IdDataPair(cv.id, cv.label) FROM CV cv", IdDataPair.class);
        Map<String, Long> cvLabelIdMap = new HashMap<>();
        for(IdDataPair cvPair : cvQuery.getResultList()) {
            cvLabelIdMap.put((String) cvPair.getData(), cvPair.getId());
        }

        // assign id's from pre-existing entities
        for(Property prop : properties.values()) {
            if(prop.getId() == null && propAccessionIdMap.containsKey(prop.getAccession())) {
                prop.setId(propAccessionIdMap.get(prop.getAccession()));
                LOGGER.trace("Duplicate property <{}>: assign id <{}>", prop.getAccession(), prop.getId());
            }
            CV cv = prop.getCv();
            if(cv.getId() == null && cvLabelIdMap.containsKey(cv.getLabel())) {
                cv.setId(cvLabelIdMap.get(cv.getLabel()));
                LOGGER.trace("Duplicate cv <label={}>: assign id <{}>", cv.getLabel(), cv.getId());
            }
        }
    }

    /**
     * Write the given {@link Property} to the database.
     *
     * If a {@code Property} with the same accession was already present in the database, an {@link IllegalArgumentException} will be thrown.
     *
     * The {@link Value}s associated with the given {@code Property} will <em>not</em> be written to the database.
     * The {@link CV} used to define the {@code Property} on the other hand will be written to the database or updated if it is already present.
     *
     * @param property  the {@code Property} that will be written to the database, not {@code null}
     */
    public void writeProperty(Property property) {
        if(property != null) {
            LOGGER.debug("Store property <{}>", property.getAccession());

            EntityManager entityManager = createEntityManager();

            try {
                // cancel if the property is already in the database
                TypedQuery<Long> query = entityManager.createQuery("SELECT prop.id FROM Property prop WHERE prop.accession = :accession", Long.class);
                query.setParameter("accession", property.getAccession());
                query.setMaxResults(1);    // restrict to a single result
                List<Long> result = query.getResultList();
                if(!result.isEmpty()) {
                    LOGGER.error("Property <{}> already exists with id <{}>", property.getAccession(), result.get(0));
                    throw new IllegalArgumentException("Property <" + property.getAccession() + " already exists with id <" + result.get(0) + ">");
                }

                // make sure a pre-existing cv is updated
                assignDuplicateCvId(property.getCv(), entityManager);

                // store this property
                entityManager.getTransaction().begin();
                entityManager.merge(property);
                entityManager.getTransaction().commit();
            } catch(EntityExistsException e) {
                LOGGER.error("Unable to store property <{}>: {}", property.getAccession(), e.getMessage());

                try {
                    LOGGER.debug("Rollback because property <{}> already exists in the database: {}", property.getAccession(), e.getMessage());
                    entityManager.getTransaction().rollback();
                } catch(PersistenceException p) {
                    LOGGER.debug("Unable to rollback for property <{}>: {}", property.getAccession(), p.getMessage(), p);
                }

                throw new IllegalArgumentException("Unable to store property <" + property.getAccession() + ">", e);
            } catch(RollbackException e) {
                LOGGER.error("Unable to store property <{}>: {}", property.getAccession(), e.getMessage());
                throw new IllegalArgumentException("Unable to store property <" + property.getAccession() + ">", e);
            } finally {
                entityManager.close();
            }
        } else {
            LOGGER.error("Unable to store <null> property");
            throw new NullPointerException("Unable to persist <null> property");
        }
    }

    /**
     * Write the given {@link CV} to the database.
     *
     * If a {@code CV} with the same label was already present in the database, it will be updated to the given {@code CV}.
     *
     * @param cv  the {@code CV} that will be written to the database, not {@code null}
     */
    public void writeCv(CV cv) {
        if(cv != null) {
            LOGGER.debug("Store cv <{}>", cv.getLabel());

            EntityManager entityManager = createEntityManager();

            try {
                assignDuplicateCvId(cv, entityManager);

                // store this cv
                entityManager.getTransaction().begin();
                entityManager.merge(cv);
                entityManager.getTransaction().commit();
            } catch(EntityExistsException e) {
                LOGGER.error("Unable to store cv <{}>: {}", cv.getLabel(), e.getMessage());

                try {
                    LOGGER.debug("Rollback because cv <{}> already exists in the database: {}", cv.getLabel(), e.getMessage());
                    entityManager.getTransaction().rollback();
                } catch(PersistenceException p) {
                    LOGGER.debug("Unable to rollback for cv <{}>: {}", cv.getLabel(), p.getMessage(), p);
                }

                throw new IllegalArgumentException("Unable to store cv <" + cv.getLabel() + ">", e);
            } catch(RollbackException e) {
                LOGGER.error("Unable to store cv <{}>: {}", cv.getLabel(), e.getMessage());
                throw new IllegalArgumentException("Unable to store cv <" + cv.getLabel() + ">", e);
            } finally {
                entityManager.close();
            }
        } else {
            LOGGER.error("Unable to store <null> cv");
            throw new NullPointerException("Unable to persist <null> cv");
        }
    }

    /**
     * Remove the {@link Event} that occurred on the {@link Instrument} with the given name on the given date from the database.
     *
     * If there is no {@code Event} in the database with these characteristics, nothing is removed from the database.
     *
     * @param instrumentName  the name of the {@code Instrument} on which the {@code Event} occurred
     * @param eventDate  the date on which the {@code Event} occurred
     */
    public void removeEvent(String instrumentName, Timestamp eventDate) {
        if(instrumentName != null && eventDate != null) {
            LOGGER.debug("Remove event <{}> for instrument <{}>", eventDate, instrumentName);

            EntityManager entityManager = createEntityManager();

            try {
                // get the event
                TypedQuery<Event> query = entityManager.createQuery("SELECT e FROM Event e WHERE e.date = :date AND e.instrument.name = :name", Event.class);
                query.setParameter("date", eventDate);
                query.setParameter("name", instrumentName);
                Event event = query.getSingleResult();

                // remove the event
                entityManager.getTransaction().begin();
                entityManager.remove(event);
                entityManager.getTransaction().commit();

            } catch(NoResultException e) {
                LOGGER.debug("Event <{}> for instrument <{}> not found in the database", eventDate, instrumentName, e);
            } catch(RollbackException e) {
                LOGGER.error("Unable to remove event <{}> for instrument <{}>: {}", eventDate, instrumentName, e.getMessage());
                throw new IllegalArgumentException("Unable to remove event <" + eventDate + ">", e);
            } finally {
                entityManager.close();
            }
        } else {
            if(instrumentName == null) {
                LOGGER.error("Unable to remove event with <null> instrument name");
            }
            if(eventDate == null) {
                LOGGER.error("Unable to remove event with <null> date");
            }
            throw new NullPointerException("Unable to remove <null> event");
        }
    }
}
