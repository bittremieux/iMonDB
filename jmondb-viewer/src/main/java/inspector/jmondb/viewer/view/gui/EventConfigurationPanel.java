package inspector.jmondb.viewer.view.gui;

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

import inspector.jmondb.model.EventType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;

public class EventConfigurationPanel {

    private JPanel panel;

    private JCheckBox checkBoxUndefined;
    private JCheckBox checkBoxCalibration;
    private JCheckBox checkBoxMaintenance;
    private JCheckBox checkBoxIncident;

    public EventConfigurationPanel() {
        panel = new JPanel(new GridLayout(0, 1));

        panel.add(new JLabel("Show events:"));

        checkBoxUndefined = new JCheckBox("Undefined");
        checkBoxUndefined.setSelected(true);
        panel.add(checkBoxUndefined);

        checkBoxCalibration = new JCheckBox("Calibration");
        checkBoxCalibration.setSelected(true);
        panel.add(checkBoxCalibration);

        checkBoxMaintenance = new JCheckBox("Maintenance");
        checkBoxMaintenance.setSelected(true);
        panel.add(checkBoxMaintenance);

        checkBoxIncident = new JCheckBox("Incident");
        checkBoxIncident.setSelected(true);
        panel.add(checkBoxIncident);
    }

    public JPanel getPanel() {
        return panel;
    }

    public EventType getCheckBoxEventType(JCheckBox checkBox) {
        EventType category = null;
        if(checkBox == checkBoxUndefined) {
            category = EventType.UNDEFINED;
        } else if(checkBox == checkBoxCalibration) {
            category = EventType.CALIBRATION;
        } else if(checkBox == checkBoxMaintenance) {
            category = EventType.MAINTENANCE;
        } else if(checkBox == checkBoxIncident) {
            category = EventType.INCIDENT;
        }

        return category;
    }

    public boolean checkBoxIsSelected(EventType type) {
        switch(type) {
            case UNDEFINED:
                return checkBoxUndefined.isSelected();
            case CALIBRATION:
                return checkBoxCalibration.isSelected();
            case MAINTENANCE:
                return checkBoxMaintenance.isSelected();
            case INCIDENT:
                return checkBoxIncident.isSelected();
            default:
                return false;
        }
    }

    public void addCheckBoxListener(ItemListener listener) {
        checkBoxUndefined.addItemListener(listener);
        checkBoxCalibration.addItemListener(listener);
        checkBoxMaintenance.addItemListener(listener);
        checkBoxIncident.addItemListener(listener);
    }
}
