package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.GraphController;
import inspector.jmondb.viewer.controller.SearchSettingsController;
import inspector.jmondb.viewer.model.DatabaseConnection;
import inspector.jmondb.viewer.view.gui.ValuePlot;
import inspector.jmondb.viewer.view.gui.ViewerFrame;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GraphAdvanceListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private SearchSettingsController searchSettingsController;
    private GraphController graphController;

    public GraphAdvanceListener(ViewerFrame viewerFrame, SearchSettingsController searchSettingsController,
                                GraphController graphController) {
        this.viewerFrame = viewerFrame;
        this.searchSettingsController = searchSettingsController;
        this.graphController = graphController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(DatabaseConnection.getConnection().isActive()) {
            // change property combobox
            boolean isAdvanced = false;
            if(e.getActionCommand().equals("Next") && searchSettingsController.hasNextProperty()) {
                searchSettingsController.advanceProperty(true);
                isAdvanced = true;
            } else if(e.getActionCommand().equals("Previous") && searchSettingsController.hasPreviousProperty()) {
                searchSettingsController.advanceProperty(false);
                isAdvanced = true;
            }

            if(isAdvanced) {
                // show the new graph
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

                // edit button state
                viewerFrame.getGraphPanel().setNextButtonEnabled(searchSettingsController.hasNextProperty());
                viewerFrame.getGraphPanel().setPreviousButtonEnabled(searchSettingsController.hasPreviousProperty());
            }
        }
    }
}
