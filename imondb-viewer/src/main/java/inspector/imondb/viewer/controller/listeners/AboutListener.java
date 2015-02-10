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

import inspector.imondb.viewer.view.gui.JLabelLink;
import inspector.imondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AboutListener implements ActionListener {

    private ViewerFrame viewerFrame;

    public AboutListener(ViewerFrame viewerFrame) {
        this.viewerFrame = viewerFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JLabelLink linkAbout = new JLabelLink("<b>iMonDB Viewer v" +
                getClass().getPackage().getImplementationVersion() +
                "</b><br><br>For more information, please visit our ", "website",
                "https://bitbucket.org/proteinspector/imondb", ".");

        JOptionPane.showMessageDialog(viewerFrame.getFrame(), linkAbout.getLabel(), "About",
                JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getClass().getResource("/images/logo-64.png")));
    }
}
