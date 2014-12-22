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

import java.io.UnsupportedEncodingException;

public class DefaultInstrumentReader implements InstrumentReader {

    protected final String EXE_TEXT_ENCODING;

    public DefaultInstrumentReader(String encoding) {
        EXE_TEXT_ENCODING = encoding;
    }

    public boolean isHeader(String line) {
        return false;
    }

    public String getHeader(String line, String oldHeader) throws UnsupportedEncodingException {
        return line;
    }

    public String[] getNameAndValue(String line) throws UnsupportedEncodingException {
        return line.split("\t");
    }
}
