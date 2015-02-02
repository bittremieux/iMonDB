package inspector.jmondb.viewer.view.gui;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

public class GraphPanel {

    private ChartPanel panel;

    public GraphPanel() {
        panel = new ChartPanel(null);
    }

    public ChartPanel getPanel() {
        return panel;
    }

    public void addEventMarker(ValueMarker marker) {
        if(panel.getChart() != null) {
            XYPlot plot = (XYPlot) panel.getChart().getPlot();
            plot.addDomainMarker(marker);
        }
    }

    public void removeEventMarker(ValueMarker marker) {
        if(panel.getChart() != null) {
            XYPlot plot = (XYPlot) panel.getChart().getPlot();
            plot.removeDomainMarker(marker);
        }
    }

    public void clearChart() {
        panel.setChart(null);
    }

    public void displayChart(JFreeChart chart) {
        panel.setChart(chart);
    }
}
