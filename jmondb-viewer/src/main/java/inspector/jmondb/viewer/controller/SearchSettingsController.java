package inspector.jmondb.viewer.controller;

import com.google.common.collect.ImmutableMap;
import inspector.jmondb.model.Instrument;
import inspector.jmondb.model.Metadata;
import inspector.jmondb.model.Property;
import inspector.jmondb.viewer.model.DatabaseConnection;
import inspector.jmondb.viewer.viewmodel.InstrumentsViewModel;
import inspector.jmondb.viewer.viewmodel.MetadataFilter;
import inspector.jmondb.viewer.viewmodel.MetadataViewModel;
import inspector.jmondb.viewer.viewmodel.PropertiesViewModel;

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

    public void clearMetadata() {
        metadataViewModel.clearAll();
    }

    public void setAllInstruments() {
        // remove old instruments
        clearInstruments();

        if(DatabaseConnection.getConnection().isActive()) {
            // retrieve all instrument names from the database
            List<String> instruments = DatabaseConnection.getConnection().getReader().getFromCustomQuery(
                    "SELECT inst.name FROM Instrument inst ORDER BY inst.name", String.class);
            instruments.forEach(instrumentsViewModel::add);
        }
    }

    public void setPropertiesForActiveInstrument() {
        // set the properties for the active instrument
        List<Property> properties = getProperties(instrumentsViewModel.getActiveInstrument());
        properties.forEach(propertiesViewModel::add);
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
