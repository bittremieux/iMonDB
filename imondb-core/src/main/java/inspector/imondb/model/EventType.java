package inspector.imondb.model;

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

public enum EventType {

    UNDEFINED("undefined"),
    CALIBRATION("calibration"),
    MAINTENANCE("maintenance"),
    INCIDENT("incident"),
    TEMPERATURE("temperature"),
    MOTION("motion");

    private final String type;

    EventType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public static EventType fromString(String text) {
        if(text != null) {
            for(EventType event : values()) {
                if(text.equals(event.toString())) {
                    return event;
                }
            }
        }

        return null;
    }
}
