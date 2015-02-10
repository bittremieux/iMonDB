package inspector.imondb.convert;

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

import inspector.imondb.model.InstrumentModel;

import java.sql.Timestamp;

/**
 * Helper class to group raw file meta data, such as the sample date and the instrument model.
 */
public class RawFileMetadata {

    private final Timestamp date;
    private final InstrumentModel model;

    public RawFileMetadata(Timestamp date, InstrumentModel model) {
        this.date = new Timestamp(date.getTime());
        this.model = model;
    }

    public Timestamp getDate() {
        return new Timestamp(date.getTime());
    }

    public InstrumentModel getModel() {
        return model;
    }
}
