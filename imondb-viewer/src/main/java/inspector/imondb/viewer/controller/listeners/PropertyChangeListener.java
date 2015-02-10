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

import inspector.imondb.viewer.controller.SearchSettingsController;
import inspector.imondb.viewer.view.gui.ViewerFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PropertyChangeListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private SearchSettingsController searchSettingsController;

    public PropertyChangeListener(ViewerFrame viewerFrame, SearchSettingsController searchSettingsController) {
        this.viewerFrame = viewerFrame;
        this.searchSettingsController = searchSettingsController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // edit button state
        viewerFrame.getGraphPanel().setNextButtonEnabled(searchSettingsController.hasNextProperty());
        viewerFrame.getGraphPanel().setPreviousButtonEnabled(searchSettingsController.hasPreviousProperty());
    }
}
