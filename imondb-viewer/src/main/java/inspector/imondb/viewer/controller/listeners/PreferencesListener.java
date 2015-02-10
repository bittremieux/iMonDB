package inspector.imondb.viewer.controller.listeners;

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

import inspector.imondb.model.EventType;
import inspector.imondb.viewer.model.DatabaseConfiguration;
import inspector.imondb.viewer.model.VisualizationConfiguration;
import inspector.imondb.viewer.view.gui.ConfigTabbedPane;
import inspector.imondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PreferencesListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private VisualizationConfiguration visualizationConfiguration;
    private DatabaseConfiguration databaseConfiguration;

    public PreferencesListener(ViewerFrame viewerFrame, VisualizationConfiguration visualizationConfiguration,
                               DatabaseConfiguration databaseConfiguration) {
        this.viewerFrame = viewerFrame;

        this.visualizationConfiguration = visualizationConfiguration;
        this.databaseConfiguration = databaseConfiguration;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigTabbedPane configPane = new ConfigTabbedPane(viewerFrame, visualizationConfiguration, databaseConfiguration);

        String[] options = new String[] { "OK", "Cancel", "Reset" };
        int option = JOptionPane.showOptionDialog(viewerFrame.getFrame(), configPane.getTabbedPane(),
                "Preferences", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if(option == JOptionPane.OK_OPTION) {
            // update preferences
            for(EventType type : EventType.values()) {
                visualizationConfiguration.setColor(type, configPane.getVisualizationConfigPanel().getColor(type));
            }
            databaseConfiguration.setAutoConnect(configPane.getDatabaseConfigPanel().getAutoConnect());
            databaseConfiguration.setHost(configPane.getDatabaseConfigPanel().getDatabaseConnectionPanel().getHost());
            databaseConfiguration.setPort(configPane.getDatabaseConfigPanel().getDatabaseConnectionPanel().getPort());
            databaseConfiguration.setUserName(configPane.getDatabaseConfigPanel().getDatabaseConnectionPanel().getUserName());
            databaseConfiguration.setPassword(configPane.getDatabaseConfigPanel().getDatabaseConnectionPanel().getPassword());
            databaseConfiguration.setDatabase(configPane.getDatabaseConfigPanel().getDatabaseConnectionPanel().getDatabase());

            refresh();
        } else if(option == JOptionPane.CANCEL_OPTION) {
            // reset preferences
            visualizationConfiguration.reset();
            databaseConfiguration.reset();

            refresh();
        }
    }

    private void refresh() {
        // refresh visualizations
        viewerFrame.getEventPanel().getEventTree().refreshColor();
        viewerFrame.getGraphPanel().getEventMarkers().refreshColor();
    }
}
