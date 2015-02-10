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

/**
 * Specifies methods to parse instrument settings from the output of stand-alone executables.
 */
public interface InstrumentReader {

    /**
     * Specifies whether the given line is a block header.
     *
     * @param line  the line that will be checked if it is a block header
     * @return {@code true} if the given line is a block header, {@code false} if not
     */
    public boolean isHeader(String line);

    /**
     * Returns the block header specified by the current line and (optionally) the previous block header.
     *
     * @param line  the line that contains the new block header
     * @param oldHeader  the previous block header
     * @return the block header specified by the current line and (optionally) the previous block header
     * @throws UnsupportedEncodingException
     */
    public String getHeader(String line, String oldHeader) throws UnsupportedEncodingException;

    /**
     * Returns an array of the settings' name and value.
     *
     * @param line  the line that contains the settings' information
     * @return an array of size two with as first element the settings' name and as second element the settings' value
     * @throws UnsupportedEncodingException
     */
    public String[] getNameAndValue(String line) throws UnsupportedEncodingException;
}
