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

import inspector.jmondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GraphSaveListener implements ActionListener {

    private ViewerFrame viewerFrame;

    public GraphSaveListener(ViewerFrame viewerFrame) {
        this.viewerFrame = viewerFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(viewerFrame.getGraphPanel().getChartPanel().getChart() != null) {
            Thread graphSaver = new Thread() {
                @Override
                public void run() {
                    try {
                        viewerFrame.getGraphPanel().getChartPanel().doSaveAs();
                    } catch(IOException ex) {
                        JOptionPane.showMessageDialog(viewerFrame.getFrame(),
                                "<html><b>Could not save the graph</b></html>\n" + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            graphSaver.start();
        } else {
            JOptionPane.showMessageDialog(viewerFrame.getFrame(), "No graph available.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
}
