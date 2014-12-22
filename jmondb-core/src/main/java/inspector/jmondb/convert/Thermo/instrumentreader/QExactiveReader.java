package inspector.jmondb.convert.thermo.instrumentreader;

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

import inspector.jmondb.model.InstrumentModel;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Parses instrument settings for the {@link InstrumentModel#THERMO_Q_EXACTIVE} instrument.
 */
public class QExactiveReader extends DefaultInstrumentReader {

    public QExactiveReader(String encoding) {
        super(encoding);
    }

    public boolean isHeader(String line) {
        String[] lineSplit = line.split("\t");

        return lineSplit.length <= 1 && line.contains("===");
    }

    public String getHeader(String line, String oldHeader) throws UnsupportedEncodingException {
        String result = line.substring(line.indexOf(' '), line.indexOf(':')).trim();
        return new String(result.getBytes(EXE_TEXT_ENCODING), Charset.forName(EXE_TEXT_ENCODING));
    }

    public String[] getNameAndValue(String line) throws UnsupportedEncodingException {
        String[] values = line.split("\t");

        String name = values[0].trim();
        if(name.contains(":")) {
            name = name.substring(0, name.lastIndexOf(':'));
        }
        String value = values.length > 1 ? values[1].trim() : "";

        return new String[] { new String(name.getBytes(EXE_TEXT_ENCODING), Charset.forName(EXE_TEXT_ENCODING)), value };
    }
}
