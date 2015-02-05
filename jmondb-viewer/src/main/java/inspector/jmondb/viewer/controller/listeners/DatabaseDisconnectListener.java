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

import inspector.jmondb.viewer.controller.DatabaseController;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DatabaseDisconnectListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private DatabaseController databaseController;

    public DatabaseDisconnectListener(ViewerFrame viewerFrame, DatabaseController databaseController) {
        this.viewerFrame = viewerFrame;
        this.databaseController = databaseController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        databaseController.disconnect();

        // indicate not connected status
        viewerFrame.getDatabasePanel().setNotConnected();
    }
}
