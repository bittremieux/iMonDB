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

import com.google.common.collect.ImmutableMap;
import inspector.imondb.collector.model.MetadataMap;
import inspector.imondb.collector.model.RegexSource;

import java.util.*;

public class MetadataConfiguration {

    private Map<String, Object> rootMap;

    public MetadataConfiguration(Map<String, Object> map) {
        rootMap = map;
    }

    public Collection<MetadataMap> getMetadata() {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> temp = (List<Map<String, String>>) rootMap.get("metadata");

        if(temp != null) {
            List<MetadataMap> result = new ArrayList<>(temp.size());
            for(Map<String, String> map : temp) {
                String key = map.get("name");
                String value = map.get("value");
                RegexSource source = RegexSource.fromString(map.get("regex-source"));
                String regex = map.get("regex");
                result.add(new MetadataMap(key, value, source, regex));
            }

            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public void setMetadata(Collection<MetadataMap> metadataMaps) {
        // replace all metadata
        List<Map<String, String>> metadata = new ArrayList<>();
        rootMap.put("metadata", metadata);

        for(MetadataMap md : metadataMaps) {
            Map<String, String> map = ImmutableMap.of("name", md.getKey(), "value", md.getValue(),
                    "regex-source", md.getSource().toString(), "regex",  md.getRegex());
            metadata.add(map);
        }
    }
}
