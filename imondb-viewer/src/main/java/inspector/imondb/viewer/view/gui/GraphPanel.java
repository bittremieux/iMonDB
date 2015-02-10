package inspector.imondb.viewer.view.gui;

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

import inspector.imondb.viewer.model.VisualizationConfiguration;
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
    private EventMarkers eventMarkers;

    private JLabel logoLabel;

    private JButton previous;
    private JButton next;
    private JLabel propertyLabel;

    public GraphPanel(VisualizationConfiguration configuration) {
        panel = new JPanel(new BorderLayout());

        // chart
        chartPanel = new ChartPanel(null);
        chartPanel.setLayout(new BorderLayout());
        logoLabel = new JLabel(new AlphaIcon(new ImageIcon(getClass().getResource("/images/logo.png")), 0.1F));
        chartPanel.add(logoLabel, BorderLayout.CENTER);
        panel.add(chartPanel, BorderLayout.CENTER);

        // markers
        eventMarkers = new EventMarkers(this, configuration);

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

    public EventMarkers getEventMarkers() {
        return eventMarkers;
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
        logoLabel.setVisible(true);
    }

    public void displayChart(JFreeChart chart) {
        logoLabel.setVisible(false);
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
