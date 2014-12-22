package inspector.jmondb.model;

/*
 * #%L
 * jMonDB Core
 * %%
 * Copyright (C) 2014 InSPECtor
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

import com.google.common.collect.ImmutableSortedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.annotations.SortNatural;
import org.hibernate.proxy.HibernateProxyHelper;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * An {@code Instrument} represents a specific mass spectrometry instrument.
 *
 * An {@code Instrument} is defined by an instrument type (specified by a {@link CV} and a user-specified name for this specific instrument.
 */
@Entity
@Access(AccessType.FIELD)
@Table(name = "imon_instrument")
public class Instrument {

    @Transient
    private static final Logger LOGGER = LogManager.getLogger(Value.class);

    /** read-only iMonDB primary key; generated by JPA */
    @Id
    @Column(name="id", nullable=false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    /** the instrument name */
    @Column(name="name", nullable=false, unique=true, length=100)
    private String name;
    /** the {@link InstrumentModel} ({@link InstrumentModel#toString()} returns the accession number) */
    @Column(name="type", nullable=false, length=10)
    private InstrumentModel type;
    /** the {@link CV} that contains the instrument description */
    @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER)
    @JoinColumn(name="l_imon_cv_id", nullable=false, referencedColumnName="id")
    private CV cv;

    @OneToMany(cascade=CascadeType.REMOVE, fetch=FetchType.LAZY, mappedBy="instrument")
    @MapKey(name="date")
    @OrderBy("date ASC")
    @SortNatural
    private SortedMap<Timestamp, Event> events;

    @OneToMany(cascade=CascadeType.REMOVE, fetch=FetchType.LAZY, mappedBy="instrument")
    @MapKey(name="sampleDate")
    @OrderBy("sampleDate ASC")
    @SortNatural
    private SortedMap<Timestamp, Run> runsPerformedOnInstrument;

    @ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.LAZY)
    @JoinTable(name="imon_instrument_properties",
            joinColumns={@JoinColumn(name="l_imon_instrument_id", referencedColumnName="id")},
            inverseJoinColumns={@JoinColumn(name="l_imon_property_id", referencedColumnName="id")})
    @MapKey(name="accession")
    private Map<String, Property> properties;

    /**
     * Default constructor required by JPA.
     * Protected access modification enforces class immutability.
     */
    protected Instrument() {
        events = new TreeMap<>();
        runsPerformedOnInstrument = new TreeMap<>();
        properties = new HashMap<>();
    }

    /**
     * Creates a specific {@link Instrument}.
     *
     * An {@code Instrument} is of a specific type (i.e. Orbitrap, Q-Exactive) and has a specific name (user-dependent).
     *
     * @param name  the instrument name, not {@code null}
     * @param type  the {@link InstrumentModel}, not {@code null}
     * @param cv  the {@link CV} that contains the instrument description, not {@code null}
     */
    public Instrument(String name, InstrumentModel type, CV cv) {
        this();

        setName(name);
        setType(type);
        setCv(cv);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        if(name != null) {
            this.name = name;
        } else {
            LOGGER.error("The instrument's name is not allowed to be <null>");
            throw new NullPointerException("The instrument's name is not allowed to be <null>");
        }
    }

    public InstrumentModel getType() {
        return type;
    }

    private void setType(InstrumentModel type) {
        if(type != null) {
            this.type = type;
        } else {
            LOGGER.error("The instrument's type is not allowed to be <null>");
            throw new NullPointerException("The instrument's type is not allowed to be <null>");
        }
    }

    public CV getCv() {
        return cv;
    }

    private void setCv(CV cv) {
        if(cv != null) {
            this.cv = cv;
        } else {
            LOGGER.error("The instrument's CV is not allowed to be <null>");
            throw new NullPointerException("The instrument's CV is not allowed to be <null>");
        }
    }

    /**
     * Returns the {@link Run} that was performed on this {@code Instrument} at the given time.
     *
     * @param time  the {@link Timestamp} sample date when the {@code Run} was performed
     * @return the {@code Run} that was performed on this {@code Instrument} at the given time
     */
    public Run getRun(Timestamp time) {
        return time != null && Hibernate.isInitialized(runsPerformedOnInstrument) ?
                runsPerformedOnInstrument.get(time) : null;
    }

    /**
     * Returns an {@link Iterator} over all {@link Run}s that were performed on this {@code Instrument}.
     *
     * @return an {@link Iterator} over all {@link Run}s that were performed on this {@code Instrument}
     */
    public Iterator<Run> getRunIterator() {
        return Hibernate.isInitialized(runsPerformedOnInstrument) ?
                runsPerformedOnInstrument.values().iterator(): Collections.emptyIterator();
    }

    /**
     * Returns all {@link Run}s that were performed on this {@code Instrument} in the given time period.
     * The time period is from {@code startTime} included until {@code stopTime} excluded.
     *
     * <em>Attention!</em> Underlying the whole range of {@code Run}s will be accessed for conversion to a {@link TreeMap}.
     * Therefore it is discouraged to call this method when the amount of {@code Run}s might be high.
     *
     * @param startTime  start time (inclusive) of the {@code Run}s to be included, not {@code null}
     * @param stopTime  stop time (excluded) of the {@code Run}s to be included, not {@code null}
     * @return a {@link SortedMap} of {@code Run}s that were performed on this {@code Instrument} in the given time period
     */
    public SortedMap<Timestamp, Run> getRunRange(Timestamp startTime, Timestamp stopTime) {
        if(startTime != null && stopTime != null) {
            if(Hibernate.isInitialized(runsPerformedOnInstrument)) {
                return runsPerformedOnInstrument.subMap(startTime, stopTime);
            } else {
                return ImmutableSortedMap.of();
            }
        } else {
            if(startTime == null) {
                LOGGER.error("The start time is not allowed to be <null>");
                throw new NullPointerException("The start time is not allowed to be <null>");
            } else {
                LOGGER.error("The stop time is not allowed to be <null>");
                throw new NullPointerException("The stop time is not allowed to be <null>");
            }
        }
    }

    /**
     * Adds the given {@link Run} to this {@code Instrument}.
     *
     * If the {@code Instrument} previously contained a {@code Run} performed at the same time, the old {@code Run} is replaced.
     *
     * A {@code Run} is automatically added to its {@code Instrument} upon its instantiation.
     *
     * @param run  the {@code Run} that is added to this {@code Instrument}, not {@code null}
     */
    void addRun(Run run) {
        if(run != null) {
            if(!Hibernate.isInitialized(runsPerformedOnInstrument)) {
                runsPerformedOnInstrument = new TreeMap<>();
            }
            runsPerformedOnInstrument.put(run.getSampleDate(), run);
        } else {
            LOGGER.error("Can't add a <null> run to the instrument");
            throw new NullPointerException("Can't add a <null> run to the instrument");
        }
    }

    /**
     * Returns the {@link Event} for this {@code Instrument} that occurred at the given time.
     *
     * @param time  the {@link Timestamp} date when the {@code Event} was occurred
     * @return the {@code Event} that occurred on this {@code Instrument} at the given time
     */
    public Event getEvent(Timestamp time) {
        return time != null && Hibernate.isInitialized(events) ? events.get(time) : null;
    }

    /**
     * Returns an {@link Iterator} over all {@link Event}s that occurred on this {@code Instrument}.
     *
     * @return an {@link Iterator} over all {@link Event}s that occurred on this {@code Instrument}
     */
    public Iterator<Event> getEventIterator() {
        return Hibernate.isInitialized(events) ? events.values().iterator() : Collections.emptyIterator();
    }

    /**
     * Returns all {@link Event}s that occurred on this {@code Instrument} in the given time period.
     * The time period is from {@code startTime} included until {@code stopTime} excluded.
     *
     * <em>Attention!</em> Underlying the whole range of {@code Event}s will be accessed for conversion to a {@link TreeMap}.
     * Therefore it is discouraged to call this method when the amount of {@code Event}s might be high.
     *
     * @param startTime  start time (inclusive) of the {@code Event}s to be included, not {@code null}
     * @param stopTime  stop time (excluded) of the {@code Event}s to be included, not {@code null}
     * @return a {@link SortedMap} of {@code Event}s that occurred on this {@code Instrument} in the given time period
     */
    public SortedMap<Timestamp, Event> getEventRange(Timestamp startTime, Timestamp stopTime) {
        if(startTime != null && stopTime != null) {
            if(Hibernate.isInitialized(events)) {
                return events.subMap(startTime, stopTime);
            } else {
                return ImmutableSortedMap.of();
            }
        } else {
            if(startTime == null) {
                LOGGER.error("The start time is not allowed to be <null>");
                throw new NullPointerException("The start time is not allowed to be <null>");
            } else {
                LOGGER.error("The stop time is not allowed to be <null>");
                throw new NullPointerException("The stop time is not allowed to be <null>");
            }
        }
    }

    /**
     * Adds the given {@link Event} to this {@code Instrument}.
     *
     * If the {@code Instrument} previously contained a {@code Event} at the same time, the old {@code Event} is replaced.
     *
     * An {@code Event} is automatically added to its {@code Instrument} upon its instantiation.
     *
     * @param event  the {@code Event} that is added to this {@code Instrument}, not {@code null}
     */
    void addEvent(Event event) {
        if(event != null) {
            if(!Hibernate.isInitialized(events)) {
                events = new TreeMap<>();
            }
            events.put(event.getDate(), event);
        } else {
            LOGGER.error("Can't add a <null> event to the instrument");
            throw new NullPointerException("Can't add a <null> event to the instrument");
        }
    }

    /**
     * Returns the {@link Property} with the given accession that was assigned to this {@code Instrument}.
     *
     * @param accession  the accession of the requested {@code Property}
     * @return the {@code Property} with the given accession that was assigned to this {@code Instrument}
     */
    public Property getProperty(String accession) {
        return accession != null && Hibernate.isInitialized(properties) ? properties.get(accession) : null;
    }

    /**
     * Returns an {@link Iterator} over all {@link Property}s that are assigned to this {@code Instrument}.
     *
     * @return an {@link Iterator} over all {@link Property}s that are assigned to this {@code Instrument}
     */
    public Iterator<Property> getPropertyIterator() {
        return Hibernate.isInitialized(properties) ? properties.values().iterator() : Collections.emptyIterator();
    }

    /**
     * Assigns the given {@link Property} to this {@code Instrument}.
     *
     * If the {@code Instrument} previously contained a {@code Property} with the same accession, the old {@code Property} is replaced.
     *
     * A {@code Property} is automatically assigned to an {@code Instrument} if a {@link Value} that is defined by the {@code Property} is added to a {@link Run} that was performed on the {@code Instrument}.
     *
     * @param property  the {@code Property} that is assigned to this {@code Instrument}, not {@code null}
     */
    void assignProperty(Property property) {
        if(property != null) {
            if(!Hibernate.isInitialized(properties)) {
                properties = new HashMap<>();
            }
            properties.put(property.getAccession(), property);
        } else {
            LOGGER.error("Can't assign a <null> property to the instrument");
            throw new NullPointerException("Can't assign a <null> property to the instrument");
        }
    }

    /**
     * Explicitly initializes containers that are loaded lazily.
     *
     * Initializing the containers is only possible if the {@code Instrument} is still attached to the JPA session that was used to retrieve it from a database.
     * Otherwise, this method has no effect.
     *
     * @param initializeEvents  indicates whether the {@link Event} container has to be initialized
     * @param initializeProperties  indicates whether the {@link Property} container has to be initialized
     */
    public void initializeContainers(boolean initializeEvents, boolean initializeProperties) {
        if(initializeEvents) {
            events.size();
        }
        if(initializeProperties) {
            properties.size();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != HibernateProxyHelper.getClassWithoutInitializingProxy(o)) {
            return false;
        }

        final Instrument that = (Instrument) o;
        return     Objects.equals(name, that.getName())
                && Objects.equals(type, that.getType())
                && Objects.equals(cv, that.getCv());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, cv);
    }
}
