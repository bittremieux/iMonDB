package inspector.imondb.model;

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

import org.junit.Test;

import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.*;

public class MetadataTest {

    private final Run run = new Run("run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()), new Instrument("name", InstrumentModel.UNKNOWN_MODEL, new CV("testCv", "Dummy CV to run the unit tests", "https://bitbucket.org/proteinspector/imondb/", "1")));

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
        Run run2 = new Run("other run", "path/to/run/", new Timestamp(Calendar.getInstance().getTime().getTime()), new Instrument("name", InstrumentModel.UNKNOWN_MODEL, new CV("testCv", "Dummy CV to run the unit tests", "https://bitbucket.org/proteinspector/imondb/", "1")));
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
