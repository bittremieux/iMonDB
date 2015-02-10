package inspector.imondb.viewer.viewmodel;

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

import inspector.imondb.model.Property;
import inspector.imondb.viewer.view.gui.PropertySelectionPanel;
import org.apache.commons.lang3.StringUtils;

public class PropertiesViewModel {

    private String propertyFilter;

    private PropertySelectionPanel propertySelectionPanel;

    public PropertiesViewModel(PropertySelectionPanel propertySelectionPanel) {
        this.propertySelectionPanel = propertySelectionPanel;
    }

    public String getPropertyFilter() {
        return propertyFilter;
    }

    public void setPropertyFilter(String filter) {
        this.propertyFilter = filter;
    }

    public void clearAll() {
        propertySelectionPanel.clearProperties();
    }

    public void add(Property property) {
        if(propertyFilter == null || StringUtils.containsIgnoreCase(property.getName(), propertyFilter)) {
            propertySelectionPanel.addProperty(property.getName(), property.getAccession());
        }
    }

    public String getActivePropertyName() {
        return hasValidProperty() ? propertySelectionPanel.getSelectedPropertyName() : null;
    }

    public String getActivePropertyAccession() {
        return hasValidProperty() ? propertySelectionPanel.getSelectedPropertyAccession() : null;
    }

    public boolean hasValidProperty() {
        return propertySelectionPanel.hasValidPropertyIndex();
    }

    public boolean hasNext() {
        return propertySelectionPanel.hasNext();
    }

    public boolean hasPrevious() {
        return propertySelectionPanel.hasPrevious();
    }

    public void advanceProperty(boolean forward) {
        propertySelectionPanel.advanceProperty(forward);
    }

    public void initializeEmptyProperty() {
        propertySelectionPanel.initializeEmptyProperty();
    }
}
