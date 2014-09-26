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

	private ArrayList<Timestamp> timestamps;

	@Before
	public void setUp() {
		final int NR_OF_RUNS = 13;

		timestamps = new ArrayList<>(NR_OF_RUNS);
		instrument = new Instrument("instrument name", InstrumentModel.UNKNOWN_MODEL, cv);

		for(int i = 0; i < NR_OF_RUNS; i++) {
			Timestamp time = null;
			try {
				time = new Timestamp(sdf.parse("01/01/20" + String.format("%02d", i)).getTime());
			} catch(ParseException ignored) {}
			timestamps.add(time);
			instrument.addRun(new Run("run_" + i, "description_" + i, time, instrument));
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
		for(Timestamp time : timestamps)
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
		Timestamp time = timestamps.get((int)(Math.random() * timestamps.size()));
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
}
