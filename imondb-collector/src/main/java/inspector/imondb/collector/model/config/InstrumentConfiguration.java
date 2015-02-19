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
import inspector.imondb.collector.model.InstrumentMap;
import inspector.imondb.collector.model.RegexSource;
import inspector.imondb.model.InstrumentModel;

import java.util.*;

public class InstrumentConfiguration {

    private Map<String, Object> rootMap;

    public InstrumentConfiguration(Map<String, Object> map) {
        rootMap = map;
    }

    public Collection<InstrumentMap> getInstruments() {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> temp = (List<Map<String, String>>) rootMap.get("instruments");

        if(temp != null) {
            List<InstrumentMap> result = new ArrayList<>(temp.size());
            for(Map<String, String> map : temp) {
                String name = map.get("name");
                InstrumentModel model = InstrumentModel.fromString(map.get("type"));
                RegexSource source = RegexSource.fromString(map.get("regex-source"));
                String regex = map.get("regex");
                result.add(new InstrumentMap(name, model, source, regex));
            }

            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public void setInstruments(Collection<InstrumentMap> instrumentMaps) {
        // replace all instruments
        List<Map<String, String>> instruments = new ArrayList<>();
        rootMap.put("instruments", instruments);

        for(InstrumentMap instrument : instrumentMaps) {
            Map<String, String> map = ImmutableMap.of("name", instrument.getKey(), "type", instrument.getValue().toString(),
                    "regex-source", instrument.getSource().toString(), "regex",  instrument.getRegex());
            instruments.add(map);
        }
    }
}
