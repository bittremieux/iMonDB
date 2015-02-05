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

public enum MetadataOperator {

    EQUAL("="),
    NOT_EQUAL("≠");

    private final String value;

    MetadataOperator(String value) {
        this.value = value;
    }

    public String toQueryString() {
        switch(this) {
            case NOT_EQUAL:
                return "!=";
            case EQUAL:
            default:
                return "=";
        }
    }

    @Override
    public String toString() {
        return value;
    }

    public static MetadataOperator fromString(String text) {
        if(text != null) {
            for(MetadataOperator operator : values()) {
                if(text.equals(operator.toString())) {
                    return operator;
                }
            }
        }

        return null;
    }
}
