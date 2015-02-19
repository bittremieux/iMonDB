package inspector.imondb.collector.model;

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

import java.util.Objects;

public class MetadataMap implements RegexMap<String> {

    private String key;
    private String value;
    private RegexSource source;
    private String regex;

    public MetadataMap(String key, String value, RegexSource source, String regex) {
        this.key = key;
        this.value = value;
        this.source = source;
        this.regex = regex;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public RegexSource getSource() {
        return source;
    }

    @Override
    public String getRegex() {
        return regex;
    }

    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(getKey()) && StringUtils.isNotBlank(getValue()) && getSource() != null && StringUtils.isNotBlank(getRegex());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        MetadataMap that = (MetadataMap) o;
        return  Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
