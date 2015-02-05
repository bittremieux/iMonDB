package inspector.jmondb.viewer.viewmodel;

/*
 * #%L
 * jMonDB Viewer
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MetadataFilter implements Iterable<Object> {

    private List<Object> items;

    public MetadataFilter() {
        items = new ArrayList<>();
    }

    public void add(Object elem) {
        if(items.size() % 2 == 0 && elem instanceof MetadataEntry) {
            items.add(elem);
        } else if(items.size() % 2 == 1 && elem instanceof MetadataConnector) {
            items.add(elem);
        } else {
            String str = items.size() % 2 == 0 ? "Entry" : "Connector";
            throw new IllegalArgumentException(str + " expected");
        }
    }

    public void toQuery(StringBuilder querySelectFrom, StringBuilder queryWhere, Map<String, String> parameters) {
        // add a connector for the first entry
        if(items.size() > 0) {
            queryWhere.append(" AND (");
        }

        int entryCount = 0;
        for(Object elem : items) {
            if(elem instanceof MetadataEntry) {
                querySelectFrom.append(" JOIN val.originatingRun.metadata md").append(entryCount);

                MetadataEntry entry = (MetadataEntry) elem;

                // add the entry
                queryWhere.append("md").append(entryCount).append(".name = :md").append(entryCount).append("Name")
                        .append(" AND md").append(entryCount).append(".value ").append(entry.getOperator().toQueryString())
                        .append(" :md").append(entryCount).append("Value");
                parameters.put("md" + entryCount + "Name", entry.getKey());
                parameters.put("md" + entryCount + "Value", entry.getValue());

                entryCount++;
            } else if(elem instanceof MetadataConnector) {
                queryWhere.append(" ").append(elem.toString()).append(" ");
            }
        }

        // close the enclosing brackets
        if(items.size() > 0) {
            queryWhere.append(")");
        }
    }

    @Override
    public Iterator<Object> iterator() {
        return items.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).toString());
            if(i < items.size() - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}
