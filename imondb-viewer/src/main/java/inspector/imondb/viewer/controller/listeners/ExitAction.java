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

import inspector.imondb.viewer.controller.DatabaseController;
import inspector.imondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

public class ExitAction extends AbstractAction {

    private ViewerFrame viewerFrame;

    private DatabaseController databaseController;

    public ExitAction(ViewerFrame viewerFrame, DatabaseController databaseController) {
        this.viewerFrame = viewerFrame;
        this.databaseController = databaseController;

        putValue(Action.NAME, "Exit");
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        viewerFrame.getFrame().dispatchEvent(new WindowEvent(viewerFrame.getFrame(), WindowEvent.WINDOW_CLOSING));
    }

    public void cleanUp() {
        databaseController.disconnect();
    }
}
