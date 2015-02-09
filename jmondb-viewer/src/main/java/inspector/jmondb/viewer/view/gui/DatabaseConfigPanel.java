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

import inspector.jmondb.viewer.model.Configuration;
import inspector.jmondb.viewer.model.DatabaseConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class DatabaseConfigPanel extends Configuration {

    private JPanel panel;

    private JCheckBox checkBox;

    private DatabaseConnectionPanel databaseConnectionPanel;

    public DatabaseConfigPanel(DatabaseConfiguration configuration) {
        panel = new JPanel(new BorderLayout());

        checkBox = new JCheckBox("Automatically connect to the database on application startup");
        checkBox.setSelected(configuration.getAutoConnect());
        checkBox.addItemListener(e -> databaseConnectionPanel.setEnabled(e.getStateChange() == ItemEvent.SELECTED));
        panel.add(checkBox, BorderLayout.PAGE_START);
        checkBox.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));

        databaseConnectionPanel = new DatabaseConnectionPanel(configuration.getHost(), configuration.getPort(),
                configuration.getUserName(), configuration.getPassword(), configuration.getDatabase());
        databaseConnectionPanel.setEnabled(getAutoConnect());
        panel.add(databaseConnectionPanel.getPanel(), BorderLayout.CENTER);
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean getAutoConnect() {
        return checkBox.isSelected();
    }

    public DatabaseConnectionPanel getDatabaseConnectionPanel() {
        return databaseConnectionPanel;
    }
}
