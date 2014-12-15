package inspector.jmondb.model;

import org.junit.Test;

import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ValueTest {

    private final CV cv = new CV("testCv", "Dummy CV to run the unit tests", "https://bitbucket.org/proteinspector/jmondb/", "1");
    private final Instrument instrument = new Instrument("name", InstrumentModel.UNKNOWN_MODEL, cv);
    private final Property property = new Property("property", "test", "accession", cv, true);
    private final Run run = new Run("run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()), instrument);

    @Test(expected = NullPointerException.class)
    public void setProperty_null() {
        new Value(null, null, null, null, null, null, null, null, null, null, null, run);
    }

    @Test(expected = NullPointerException.class)
    public void setRun_null() {
        new Value(null, null, null, null, null, null, null, null, null, null, property, null);
    }

    @Test
    public void equals() {
        Value value = new Value("1", 1, 1, 1., 1., 1., 1., 1., 1., 1., property, run);
        Property property2 = new Property("other property", "test", "accession", cv, true);
        Value valueProperty = new Value("1", 1, 1, 1., 1., 1., 1., 1., 1., 1., property2, run);
        Run run2 = new Run("other run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()), instrument);
        Value valueRun = new Value("1", 1, 1, 1., 1., 1., 1., 1., 1., 1., property, run2);
        Value valueIdentical = new Value("1", 1, 1, 1., 1., 1., 1., 1., 1., 1., property, run);
        Value valueBuilder = new ValueBuilder().setFirstValue("1").setN(1).setNDiffValues(1)
                .setMin(1.).setMax(1.).setMean(1.).setMedian(1.).setQ1(1.).setQ3(1.).setSd(1.)
                .setDefiningProperty(property).setOriginatingRun(run).createValue();

        assertEquals(value, value);
        assertNotEquals(value, null);
        assertNotEquals(value, new Object());
        assertNotEquals(value, valueProperty);
        assertNotEquals(value, valueRun);
        assertEquals(value, valueIdentical);
        assertEquals(value, valueBuilder);
    }
}
