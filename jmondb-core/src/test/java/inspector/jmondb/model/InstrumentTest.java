package inspector.jmondb.model;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SortedMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class InstrumentTest {

	private Instrument instrument;

	private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	private final CV cv = new CV("testCv", "Dummy CV to run the unit tests", "https://bitbucket.org/proteinspector/jmondb/", "1");

	private ArrayList<Timestamp> runDates;
	private ArrayList<Timestamp> eventDates;

	@Before
	public void setUp() {
		final int NR_OF_RUNS = 13;
		final int NR_OF_EVENTS = 5;

		runDates = new ArrayList<>(NR_OF_RUNS);
		eventDates = new ArrayList<>(NR_OF_EVENTS);
		instrument = new Instrument("instrument name", InstrumentModel.UNKNOWN_MODEL, cv);

		for(int i = 0; i < NR_OF_RUNS; i++) {
			try {
				Timestamp time = new Timestamp(sdf.parse("01/01/20" + String.format("%02d", i)).getTime());
				runDates.add(time);
				new Run("run_" + i, "description_" + i, time, instrument);
			} catch(ParseException ignored) {}
		}

		for(int i = 0; i < NR_OF_EVENTS; i++) {
			try {
				Timestamp time = new Timestamp(sdf.parse("17/06/20" + String.format("%02d", i)).getTime());
				eventDates.add(time);
				new Event(instrument, time, EventType.INCIDENT);
			} catch(ParseException ignored) {}
		}
	}

	@Test
	public void getRun_null() {
		assertNull(instrument.getRun(null));
	}

	@Test
	public void getRun_nonExisting() {
		assertNull(instrument.getRun(new Timestamp(new Date().getTime())));
	}

	@Test
	public void getRun_valid() {
		for(Timestamp time : runDates)
			assertNotNull(instrument.getRun(time));
	}

	@Test(expected=NullPointerException.class)
	public void getRunRange_nullStart() {
		try {
			instrument.getRunRange(null, new Timestamp(sdf.parse("01/01/2001").getTime()));
		} catch(ParseException ignored) {}
	}

	@Test(expected=NullPointerException.class)
	public void getRunRange_nullStop() {
		try {
			instrument.getRunRange(new Timestamp(sdf.parse("01/01/2001").getTime()), null);
		} catch(ParseException ignored) {}
	}

	@Test
	public void getRunRange_emptyRange() {
		try {
			SortedMap<Timestamp, Run> range = instrument.getRunRange(new Timestamp(sdf.parse("01/01/1990").getTime()), new Timestamp(sdf.parse("01/01/1995").getTime()));
			assertEquals(range.size(), 0);
		} catch(ParseException ignored) {}
	}

	@Test
	public void getRunRange_valid() {
		try {
			SortedMap<Timestamp, Run> range = instrument.getRunRange(new Timestamp(sdf.parse("01/01/2000").getTime()), new Timestamp(sdf.parse("01/01/2005").getTime()));
			assertThat(range.size(), greaterThan(0));
		} catch(ParseException ignored) {}
	}

	@Test(expected=NullPointerException.class)
	public void addRun_null() {
		instrument.addRun(null);
	}

	@Test
	public void addRun_duplicate() {
		Timestamp time = runDates.get((int)(Math.random() * runDates.size()));
		Run oldRun = instrument.getRun(time);
		assertNotNull(oldRun);

		new Run("run_new", "description_new", time, instrument);

		assertNotEquals(oldRun, instrument.getRun(time));
	}

	@Test
	public void addRun_new() {
		Timestamp time = new Timestamp(new Date().getTime());
		assertNull(instrument.getRun(time));

		new Run("run_new", "description_new", time, instrument);

		assertNotNull(instrument.getRun(time));
	}

	@Test
	public void getEvent_null() {
		assertNull(instrument.getEvent(null));
	}

	@Test
	public void getEvent_nonExisting() {
		assertNull(instrument.getEvent(new Timestamp(new Date().getTime())));
	}

	@Test
	public void getEvent_valid() {
		for(Timestamp time : eventDates)
			assertNotNull(instrument.getEvent(time));
	}

	@Test(expected=NullPointerException.class)
	public void getEventRange_nullStart() {
		try {
			instrument.getEventRange(null, new Timestamp(sdf.parse("01/01/2001").getTime()));
		} catch(ParseException ignored) {}
	}

	@Test(expected=NullPointerException.class)
	public void getEventRange_nullStop() {
		try {
			instrument.getEventRange(new Timestamp(sdf.parse("01/01/2001").getTime()), null);
		} catch(ParseException ignored) {}
	}

	@Test
	public void getEventRange_emptyRange() {
		try {
			SortedMap<Timestamp, Event> range = instrument.getEventRange(new Timestamp(sdf.parse("01/01/1990").getTime()), new Timestamp(sdf.parse("01/01/1995").getTime()));
			assertEquals(range.size(), 0);
		} catch(ParseException ignored) {}
	}

	@Test
	public void getEventRange_valid() {
		try {
			SortedMap<Timestamp, Event> range = instrument.getEventRange(new Timestamp(sdf.parse("01/01/2000").getTime()), new Timestamp(sdf.parse("01/01/2003").getTime()));
			assertThat(range.size(), greaterThan(0));
		} catch(ParseException ignored) {}
	}

	@Test(expected=NullPointerException.class)
	public void addEvent_null() {
		instrument.addEvent(null);
	}

	@Test
	public void addEvent_duplicate() {
		Timestamp time = eventDates.get((int)(Math.random() * eventDates.size()));
		Event oldEvent = instrument.getEvent(time);
		assertNotNull(oldEvent);

		new Event(instrument, time, EventType.CALIBRATION, "my problem", "my solution", null);

		assertNotEquals(oldEvent, instrument.getEvent(time));
	}

	@Test
	public void addEvent_new() {
		Timestamp time = new Timestamp(new Date().getTime());
		assertNull(instrument.getEvent(time));

		new Event(instrument, time, EventType.INCIDENT);

		assertNotNull(instrument.getEvent(time));
	}
}
