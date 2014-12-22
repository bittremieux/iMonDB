package inspector.jmondb.jpa;

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

import inspector.jmondb.model.InstrumentModel;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply=true)
public class InstrumentModelConverter implements AttributeConverter<InstrumentModel, String> {

    @Override
    public String convertToDatabaseColumn(InstrumentModel instrumentModel) {
        return instrumentModel.toString();
    }

    @Override
    public InstrumentModel convertToEntityAttribute(String s) {
        return InstrumentModel.fromString(s);
    }
}
