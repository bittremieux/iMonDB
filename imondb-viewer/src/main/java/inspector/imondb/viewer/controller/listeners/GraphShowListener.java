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

import inspector.imondb.viewer.controller.GraphController;
import inspector.imondb.viewer.controller.SearchSettingsController;
import inspector.imondb.viewer.model.DatabaseConnection;
import inspector.imondb.viewer.view.gui.ValuePlot;
import inspector.imondb.viewer.view.gui.ViewerFrame;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class GraphShowListener implements ActionListener, Observer {

    private ViewerFrame viewerFrame;

    private SearchSettingsController searchSettingsController;
    private GraphController graphController;

    public GraphShowListener(ViewerFrame viewerFrame, SearchSettingsController searchSettingsController,
                             GraphController graphController) {
        this.viewerFrame = viewerFrame;
        this.searchSettingsController = searchSettingsController;
        this.graphController = graphController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(DatabaseConnection.getConnection().isActive()) {
            // change property combobox (if necessary)
            if(e.getActionCommand().equals("Next") && searchSettingsController.hasNextProperty()) {
                searchSettingsController.advanceProperty(true);
            } else if(e.getActionCommand().equals("Previous") && searchSettingsController.hasPreviousProperty()) {
                searchSettingsController.advanceProperty(false);
            } else if(e.getActionCommand().equals("propertyChanged")) {
                showGraph();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        showGraph();
    }

    private void showGraph() {
        Thread graphThread = new Thread() {
            public void run() {
                List<Object[]> values = graphController.queryValues();

                if(values != null) {
                    if(values.size() == 0) {
                        JOptionPane.showMessageDialog(viewerFrame.getFrame(), "No matching values found.",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    } else {
                        ValuePlot plot = new ValuePlot(values);
                        JFreeChart chart = new JFreeChart(plot.getPlot());
                        chart.removeLegend();

                        graphController.display(chart);
                        graphController.displayEvents();
                    }
                }
            }
        };
        graphThread.start();
    }
}
