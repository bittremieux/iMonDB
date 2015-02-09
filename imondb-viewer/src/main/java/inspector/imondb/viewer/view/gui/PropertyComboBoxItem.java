package inspector.imondb.viewer.view.gui;

/*
 * #%L
 * iMonDB Viewer
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

public class PropertyComboBoxItem implements Comparable<PropertyComboBoxItem> {

    private String name;
    private String accession;

    public PropertyComboBoxItem(String name, String accession) {
        this.name = name;
        this.accession = accession;
    }

    public String getName() {
        return name;
    }

    public String getAccession() {
        return accession;
    }

    @Override
    public String toString() {
        return name + " (" + accession + ")";
    }

    @Override
    public int compareTo(PropertyComboBoxItem o) {
        return getName().compareTo(o.getName());
    }
}
