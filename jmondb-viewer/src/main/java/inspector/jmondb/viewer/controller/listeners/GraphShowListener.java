package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.GraphController;
import inspector.jmondb.viewer.model.DatabaseConnection;
import inspector.jmondb.viewer.view.gui.ValuePlot;
import inspector.jmondb.viewer.view.gui.ViewerFrame;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GraphShowListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private GraphController graphController;

    public GraphShowListener(ViewerFrame frame, GraphController graphController) {
        this.viewerFrame = frame;

        this.graphController = graphController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(DatabaseConnection.getConnection().isActive()) {
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
}
