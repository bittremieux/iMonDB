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

import inspector.jmondb.viewer.model.DatabaseConfiguration;
import inspector.jmondb.viewer.model.VisualizationConfiguration;

import javax.swing.*;

public class ConfigTabbedPane {

    private JTabbedPane tabbedPane;

    private VisualizationConfigPanel visualizationConfigPanel;
    private DatabaseConfigPanel databaseConfigPanel;

    public ConfigTabbedPane(ViewerFrame viewerFrame, VisualizationConfiguration visualizationConfiguration,
                            DatabaseConfiguration databaseConfiguration) {
        tabbedPane = new JTabbedPane();

        visualizationConfigPanel = new VisualizationConfigPanel(viewerFrame, visualizationConfiguration);
        JPanel visualizationPanel = visualizationConfigPanel.getPanel();
        tabbedPane.addTab("Visualization", visualizationPanel);
        visualizationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        databaseConfigPanel = new DatabaseConfigPanel(databaseConfiguration);
        JPanel databasePanel = databaseConfigPanel.getPanel();
        tabbedPane.addTab("Database", databaseConfigPanel.getPanel());
        databasePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public VisualizationConfigPanel getVisualizationConfigPanel() {
        return visualizationConfigPanel;
    }

    public DatabaseConfigPanel getDatabaseConfigPanel() {
        return databaseConfigPanel;
    }
}
