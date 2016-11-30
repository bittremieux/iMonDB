package inspector.imondb.viewer.controller;

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

import com.google.common.collect.ImmutableMap;
import inspector.imondb.model.Instrument;
import inspector.imondb.model.Metadata;
import inspector.imondb.model.Property;
import inspector.imondb.viewer.model.DatabaseConnection;
import inspector.imondb.viewer.viewmodel.InstrumentsViewModel;
import inspector.imondb.viewer.viewmodel.MetadataFilter;
import inspector.imondb.viewer.viewmodel.MetadataViewModel;
import inspector.imondb.viewer.viewmodel.PropertiesViewModel;

import java.util.*;

public class SearchSettingsController {

    private InstrumentsViewModel instrumentsViewModel;
    private PropertiesViewModel propertiesViewModel;
    private MetadataViewModel metadataViewModel;

    public SearchSettingsController(InstrumentsViewModel instrumentsViewModel, PropertiesViewModel propertiesViewModel,
                                    MetadataViewModel metadataViewModel) {
        this.instrumentsViewModel = instrumentsViewModel;
        this.propertiesViewModel = propertiesViewModel;
        this.metadataViewModel = metadataViewModel;
    }

    public void clearInstruments() {
        instrumentsViewModel.clearAll();
    }

    public void clearProperties() {
        propertiesViewModel.clearAll();
        propertiesViewModel.setPropertyFilter(null);
    }

    public boolean hasNextProperty() {
        return propertiesViewModel.hasValidProperty() && propertiesViewModel.hasNext();
    }

    public boolean hasPreviousProperty() {
        return propertiesViewModel.hasValidProperty() && propertiesViewModel.hasPrevious();
    }

    public void advanceProperty(boolean forward) {
        propertiesViewModel.advanceProperty(forward);
    }

    public void clearMetadata() {
        metadataViewModel.clearAll();
    }

    public void setAllInstruments() {
        // remove old instruments
        clearInstruments();

        if(DatabaseConnection.getConnection().isActive()) {
            // MS instruments
            List<String> instruments = DatabaseConnection.getConnection().getReader().getFromCustomQuery(
                    "SELECT inst.name FROM Instrument inst WHERE inst.type != 'external' ORDER BY inst.name", String.class);
            instruments.forEach(instrumentsViewModel::add);
            // external instruments
            instrumentsViewModel.addExternal("(none)");
            List<String> externalInstruments = DatabaseConnection.getConnection().getReader().getFromCustomQuery(
                    "SELECT inst.name FROM Instrument inst WHERE inst.type = 'external' ORDER BY inst.name", String.class);
            externalInstruments.forEach(instrumentsViewModel::addExternal);
        }
    }

    public void setPropertiesForActiveInstrument() {
        // set the properties for the active instrument
        List<Property> properties = getProperties(instrumentsViewModel.getActiveInstrument());
        properties.forEach(propertiesViewModel::add);
        propertiesViewModel.initializeEmptyProperty();
    }

    private List<Property> getProperties(String instrumentName) {
        if(DatabaseConnection.getConnection().isActive()) {
            // load the given instrument and all its properties
            Instrument instrument = DatabaseConnection.getConnection().getReader().getInstrument(
                    instrumentName, false, true);

            // alphabetically sort all (numeric) properties
            List<Property> properties = new ArrayList<>();
            for(Iterator<Property> it = instrument.getPropertyIterator(); it.hasNext(); ) {
                Property property = it.next();
                if(property.getNumeric()) {
                    properties.add(property);
                }
            }
            Collections.sort(properties, new Comparator<Property>() {
                @Override
                public int compare(Property o1, Property o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            return properties;
        } else {
            return Collections.emptyList();
        }
    }

    public void setPropertyFilter(String filter) {
        if(filter == null ? propertiesViewModel.getPropertyFilter() != null :
                !filter.equals(propertiesViewModel.getPropertyFilter())) {
            clearProperties();
            propertiesViewModel.setPropertyFilter(filter);
            setPropertiesForActiveInstrument();
        }
    }

    public void resetPropertyFilter() {
        setPropertyFilter(null);
    }

    public void setMetadataOptionsForActiveInstrument() {
        // add new metadata for the active instrument
        if(DatabaseConnection.getConnection().isActive()) {
            List<Metadata> metadata = DatabaseConnection.getConnection().getReader().getFromCustomQuery(
                    "SELECT md FROM Metadata md WHERE md.run.instrument.name = :instName", Metadata.class,
                    ImmutableMap.of("instName", instrumentsViewModel.getActiveInstrument()));
            metadata.forEach(metadataViewModel::addMetadataOption);
        }
    }

    public void setMetadataFilter(MetadataFilter filter) {
        metadataViewModel.setMetadataFilter(filter);
    }

    public void resetMetadataFilter() {
        metadataViewModel.reset();
    }

}
