package inspector.jmondb.viewer.view.gui;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GraphPanel {

    private JPanel panel;

    private ChartPanel chartPanel;

    private JButton previous;
    private JButton next;
    private JLabel propertyLabel;

    public GraphPanel() {
        panel = new JPanel(new BorderLayout());

        // chart
        chartPanel = new ChartPanel(null);
        panel.add(chartPanel, BorderLayout.CENTER);

        // previous next buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        panel.add(buttonsPanel, BorderLayout.PAGE_END);
        previous = new JButton(new ImageIcon(getClass().getResource("/images/previous.png")));
        previous.setActionCommand("Previous");
        previous.setEnabled(false);
        next = new JButton(new ImageIcon(getClass().getResource("/images/next.png")));
        next.setActionCommand("Next");
        next.setEnabled(false);

        // property label
        propertyLabel = new JLabel("", SwingConstants.CENTER);
        propertyLabel.setPreferredSize(new Dimension(450, 25));
        propertyLabel.setMaximumSize(new Dimension(450, 25));

        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(previous);
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(propertyLabel);
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(next);
        buttonsPanel.add(Box.createHorizontalGlue());
    }

    public JPanel getPanel() {
        return panel;
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public void addEventMarker(ValueMarker marker) {
        if(chartPanel.getChart() != null) {
            XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
            plot.addDomainMarker(marker);
        }
    }

    public void removeEventMarker(ValueMarker marker) {
        if(chartPanel.getChart() != null) {
            XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
            plot.removeDomainMarker(marker);
        }
    }

    public void clearChart() {
        chartPanel.setChart(null);
    }

    public void displayChart(JFreeChart chart) {
        chartPanel.setChart(chart);
    }

    public void setPreviousButtonEnabled(boolean enabled) {
        previous.setEnabled(enabled);
    }

    public void setNextButtonEnabled(boolean enabled) {
        next.setEnabled(enabled);
    }

    public void setTitle(String title) {
        propertyLabel.setText(title);
    }

    public void addGraphAdvancer(ActionListener listener) {
        previous.addActionListener(listener);
        next.addActionListener(listener);
    }
}
