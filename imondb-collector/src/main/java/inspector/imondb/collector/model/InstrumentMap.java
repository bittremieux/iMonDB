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

import inspector.imondb.model.InstrumentModel;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

public class InstrumentMap implements RegexMap<InstrumentModel> {

    private String name;
    private InstrumentModel model;
    private RegexSource source;
    private String regex;

    public InstrumentMap(String name, InstrumentModel model, RegexSource source, String regex) {
        this.name = name;
        this.model = model;
        this.source = source;
        this.regex = regex;
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public InstrumentModel getValue() {
        return model;
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
        return StringUtils.isNotBlank(getKey()) && getValue() != null && getSource() != null && StringUtils.isNotBlank(getRegex());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        InstrumentMap that = (InstrumentMap) o;
        return  Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "InstrumentMap{" +
                "name='" + name + '\'' +
                ", model=" + model +
                ", source=" + source +
                ", regex='" + regex + '\'' +
                '}';
    }
}
