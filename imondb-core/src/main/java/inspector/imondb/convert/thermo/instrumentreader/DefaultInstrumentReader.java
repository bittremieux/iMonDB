package inspector.imondb.convert.thermo.instrumentreader;

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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class DefaultInstrumentReader implements InstrumentReader {

    protected final String textEncoding;

    public DefaultInstrumentReader(String encoding) {
        textEncoding = encoding;
    }

    public boolean isHeader(String line) {
        return false;
    }

    public String getHeader(String line, String oldHeader) throws UnsupportedEncodingException {
        return line;
    }

    public String[] getNameAndValue(String line) throws UnsupportedEncodingException {
        String[] values = line.split("\t");

        String name = values[0].trim();
        String value = values.length > 1 ? values[1].trim() : "";

        return new String[] { new String(name.getBytes(textEncoding), Charset.forName(textEncoding)), value };
    }
}
