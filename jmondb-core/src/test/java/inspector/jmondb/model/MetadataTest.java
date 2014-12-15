package inspector.jmondb.model;

import org.junit.Test;

import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.*;

public class MetadataTest {

    private final Run run = new Run("run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()), new Instrument("name", InstrumentModel.UNKNOWN_MODEL, new CV("testCv", "Dummy CV to run the unit tests", "https://bitbucket.org/proteinspector/jmondb/", "1")));

    @Test(expected = NullPointerException.class)
    public void setName_null() {
        new Metadata(null, "value", run);
    }

    @Test(expected = NullPointerException.class)
    public void setValue_null() {
        new Metadata("name", null, run);
    }

    @Test(expected = NullPointerException.class)
    public void setRun_null() {
        new Metadata("name", "value", null);
    }

    @Test
    public void equals() {
        Metadata md = new Metadata("name", "value", run);
        Metadata mdName = new Metadata("other name", "value", run);
        Metadata mdValue = new Metadata("name", "other value", run);
        Run run2 = new Run("other run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()), new Instrument("name", InstrumentModel.UNKNOWN_MODEL, new CV("testCv", "Dummy CV to run the unit tests", "https://bitbucket.org/proteinspector/jmondb/", "1")));
        Metadata mdRun = new Metadata("name", "value", run2);
        Metadata mdIdentical = new Metadata("name", "value", run);

        assertEquals(md, md);
        assertNotEquals(md, null);
        assertNotEquals(md, new Object());
        assertNotEquals(md, mdName);
        assertNotEquals(md, mdValue);
        assertNotEquals(md, mdRun);
        assertEquals(md, mdIdentical);
    }
}
