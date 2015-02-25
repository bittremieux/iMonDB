package inspector.imondb.collector.model.config;

/*
 * #%L
 * iMonDB Collector
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

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class GeneralConfiguration {

    /** Date formatter to convert to and from date strings */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static final String REGEX_DEFAULT = "^.*\\.raw$";
    private static final int NR_OF_THREADS_DEFAULT = 1;
    private static final boolean UNIQUE_DEFAULT = false;

    private Map<String, Object> rootMap;

    public GeneralConfiguration(Map<String, Object> map) {
        rootMap = map;

        if(rootMap.get("general") == null) {
            rootMap.put("general", new HashMap<String, String>());
        }
    }

    public String getDirectory() {
        @SuppressWarnings("unchecked")
        String result = ((Map<String, String>) rootMap.get("general")).get("dir");
        return result;
    }

    public void setDirectory(String directory) {
        @SuppressWarnings("unchecked")
        Map<String, String> generalMap = (Map<String, String>) rootMap.get("general");
        generalMap.put("dir", directory);
    }

    public String getFileNameRegex() {
        @SuppressWarnings("unchecked")
        String result = ((Map<String, String>) rootMap.get("general")).get("match_file");
        return !StringUtils.isEmpty(result) ? result : REGEX_DEFAULT;
    }

    public void setFileNameRegex(String regex) {
        @SuppressWarnings("unchecked")
        Map<String, String> generalMap = (Map<String, String>) rootMap.get("general");
        generalMap.put("match_file", !StringUtils.isEmpty(regex) ? regex : REGEX_DEFAULT);
    }

    public Timestamp getStartDate() {
        @SuppressWarnings("unchecked")
        String result = ((Map<String, String>) rootMap.get("general")).get("last_date");
        if(result != null) {
            try {
                return new Timestamp(DATE_FORMAT.parse(result).getTime());
            } catch(ParseException ignored) {
            }
        }
        return null;
    }

    public void setStartDate(Timestamp date) {
        @SuppressWarnings("unchecked")
        Map<String, Object> generalMap = (Map<String, Object>) rootMap.get("general");
        generalMap.put("last_date", date != null ? DATE_FORMAT.format(date) : null);
    }

    public int getNumberOfThreads() {
        @SuppressWarnings("unchecked")
        Integer result = ((Map<String, Integer>) rootMap.get("general")).get("num_threads");
        return result != null && result > 0 ? result : NR_OF_THREADS_DEFAULT;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> generalMap = (Map<String, Integer>) rootMap.get("general");
        generalMap.put("num_threads", numberOfThreads > 0 ? numberOfThreads : NR_OF_THREADS_DEFAULT);
    }

    public boolean getUniqueFileNames() {
        @SuppressWarnings("unchecked")
        Boolean result = ((Map<String, Boolean>) rootMap.get("general")).get("force_unique");
        return result != null ? result : UNIQUE_DEFAULT;
    }

    public void setUniqueFileNames(boolean unique) {
        @SuppressWarnings("unchecked")
        Map<String, Boolean> generalMap = (Map<String, Boolean>) rootMap.get("general");
        generalMap.put("force_unique", unique);
    }
}
