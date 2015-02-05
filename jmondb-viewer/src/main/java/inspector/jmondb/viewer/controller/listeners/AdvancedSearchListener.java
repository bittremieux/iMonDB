package inspector.jmondb.viewer.controller.listeners;

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

import inspector.jmondb.viewer.controller.SearchSettingsController;
import inspector.jmondb.viewer.model.DatabaseConnection;
import inspector.jmondb.viewer.view.gui.SearchDialog;
import inspector.jmondb.viewer.view.gui.ViewerFrame;
import inspector.jmondb.viewer.viewmodel.MetadataViewModel;
import inspector.jmondb.viewer.viewmodel.PropertiesViewModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

public class AdvancedSearchListener extends Observable implements ActionListener {

    private ViewerFrame viewerFrame;

    private PropertiesViewModel propertiesViewModel;
    private MetadataViewModel metadataViewModel;

    private SearchSettingsController searchSettingsController;

    public AdvancedSearchListener(ViewerFrame viewerFrame, PropertiesViewModel propertiesViewModel,
                                  MetadataViewModel metadataViewModel,
                                  SearchSettingsController searchSettingsController, GraphShowListener graphShowListener) {
        this.viewerFrame = viewerFrame;
        this.propertiesViewModel = propertiesViewModel;
        this.metadataViewModel = metadataViewModel;

        this.searchSettingsController = searchSettingsController;

        addObserver(graphShowListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(DatabaseConnection.getConnection().isActive()) {
            // create dialog (optionally populated with previous filter settings)
            SearchDialog searchDialog = new SearchDialog(metadataViewModel.getMetadataOptions(),
                    propertiesViewModel.getPropertyFilter(), metadataViewModel.getMetadataFilter());

            String[] options = new String[] { "OK", "Cancel", "Reset" };
            int option = JOptionPane.showOptionDialog(viewerFrame.getFrame(), searchDialog.getPanel(),
                    "Advanced search settings", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    options, options[0]);

            if(option == JOptionPane.YES_OPTION) {
                // apply filter settings
                searchSettingsController.setPropertyFilter(searchDialog.getPropertyFilter());
                searchSettingsController.setMetadataFilter(searchDialog.getMetadata());
                // notify metadata observers
                setChanged();
            }
            else if(option == JOptionPane.CANCEL_OPTION) {
                // reset search settings
                searchSettingsController.resetPropertyFilter();
                searchSettingsController.resetMetadataFilter();
                // notify metadata observers
                setChanged();
            }
            notifyObservers();
        }
    }
}
