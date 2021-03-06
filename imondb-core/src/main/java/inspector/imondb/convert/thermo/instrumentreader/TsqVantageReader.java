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

import inspector.imondb.model.InstrumentModel;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Parses instrument settings for the {@link InstrumentModel#THERMO_TSQ_VANTAGE} instrument.
 */
public class TsqVantageReader extends DefaultInstrumentReader {

    public TsqVantageReader(String encoding) {
        super(encoding);
    }

    public boolean isHeader(String line) {
        String[] lineSplit = line.split("\t");

        return lineSplit.length <= 1;
    }

    public String getHeader(String line, String oldHeader) throws UnsupportedEncodingException {
        String blockHeader = oldHeader.contains("-") ? oldHeader.substring(0, oldHeader.indexOf('-')).trim() : oldHeader;

        if("\"".equals(line.substring(0, 1)) && blockHeader.length() > 0) {
            String result = blockHeader + " - " + line;
            return new String(result.getBytes(textEncoding), Charset.forName(textEncoding));
        } else {
            return new String(line.getBytes(textEncoding), Charset.forName(textEncoding));
        }
    }

    public String[] getNameAndValue(String line) throws UnsupportedEncodingException {
        String[] values = line.split("\t");

        String value = values.length > 1 ? values[1].trim() : "";
        return new String[] { new String(values[0].getBytes(textEncoding), Charset.forName(textEncoding)), value };
    }
}
