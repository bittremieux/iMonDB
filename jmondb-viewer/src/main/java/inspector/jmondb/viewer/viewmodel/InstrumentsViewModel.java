package inspector.jmondb.viewer.viewmodel;

import inspector.jmondb.viewer.view.gui.PropertySelectionPanel;

public class InstrumentsViewModel {

    private PropertySelectionPanel propertySelectionPanel;

    public InstrumentsViewModel(PropertySelectionPanel propertySelectionPanel) {
        this.propertySelectionPanel = propertySelectionPanel;
    }

    public void clearAll() {
        propertySelectionPanel.clearInstruments();
    }

    public void add(String instrument) {
        propertySelectionPanel.addInstrument(instrument);
    }

    public String getActiveInstrument() {
        return propertySelectionPanel.getSelectedInstrument();
    }
}
