package inspector.jmondb.viewer.viewmodel;

import inspector.jmondb.model.Property;
import inspector.jmondb.viewer.view.gui.PropertySelectionPanel;
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
        return propertySelectionPanel.getSelectedPropertyName();
    }

    public String getActivePropertyAccession() {
        return propertySelectionPanel.getSelectedPropertyAccession();
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
}
