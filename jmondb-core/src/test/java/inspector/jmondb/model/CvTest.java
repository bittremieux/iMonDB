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

import org.junit.Test;

import static org.junit.Assert.*;

public class CvTest {

    private final CV cv = new CV("my cv", "this is a cv", "https://bitbucket.org/proteinspector/jmondb/", "1");

    @Test(expected = NullPointerException.class)
    public void setLabel_null() {
        new CV(null, "this is a cv", "https://bitbucket.org/proteinspector/jmondb/", "1");
    }

    @Test(expected = NullPointerException.class)
    public void setName_null() {
        new CV("my cv", null, "https://bitbucket.org/proteinspector/jmondb/", "1");
    }

    @Test(expected = NullPointerException.class)
    public void setUri_null() {
        new CV("my cv", "this is a cv", null, "1");
    }

    @Test
    public void setVersion_null() {
        new CV("my cv", "this is a cv", "https://bitbucket.org/proteinspector/jmondb/", null);
    }

    @Test
    public void equals() {
        CV cv = new CV("my cv", "this is a cv", "https://bitbucket.org/proteinspector/jmondb/", "1");
        CV cvLabel = new CV("other label", "this is a cv", "https://bitbucket.org/proteinspector/jmondb/", "1");
        CV cvName = new CV("my cv", "other name", "https://bitbucket.org/proteinspector/jmondb/", "1");
        CV cvUri = new CV("my cv", "this is a cv", "other uri", "1");
        CV cvVersion = new CV("my cv", "this is a cv", "https://bitbucket.org/proteinspector/jmondb/", "other version");
        CV cvIdentical = new CV("my cv", "this is a cv", "https://bitbucket.org/proteinspector/jmondb/", "1");

        assertEquals(cv, cv);
        assertNotEquals(cv, null);
        assertNotEquals(cv, new Object());
        assertNotEquals(cv, cvLabel);
        assertNotEquals(cv, cvName);
        assertNotEquals(cv, cvUri);
        assertNotEquals(cv, cvVersion);
        assertEquals(cv, cvIdentical);
    }
}
