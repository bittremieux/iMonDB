package inspector.imondb.collector.view.overview;

/*
 * #%L
 * iMonDB Collector
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

import inspector.imondb.collector.controller.ExecutionController;
import inspector.imondb.collector.view.CollectorFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

public class ExecutionPanel implements Observer {

    private JPanel panel;

    private JPanel panelCardLayout;
    private JPanel panelOverview;
    private JPanel panelProgress;
    private JButton buttonExecute;

    public ExecutionPanel(CollectorFrame collectorFrame, ExecutionController executionController) {
        panelOverview.add(collectorFrame.getOverviewPanel().getPanel());
        collectorFrame.getOverviewPanel().addObserver(this);

        ProgressPanel progressPanel = new ProgressPanel(this, collectorFrame, executionController);
        panelProgress.add(progressPanel.getPanel(), BorderLayout.CENTER);

        buttonExecute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // change the card layout
                updateLayout(e);

                // execute the button functionality
                if("Start collector".equals(e.getActionCommand())) {
                    progressPanel.start();
                } else if("Stop collector".equals(e.getActionCommand())) {
                    progressPanel.stop();
                } else if("Schedule collector".equals(e.getActionCommand())) {
                    //TODO
                }
            }
        });
    }

    private void updateLayout(ActionEvent e) {
        CardLayout cardLayout = (CardLayout) panelCardLayout.getLayout();
        String cardName = "CardOverview";
        if("Start collector".equals(e.getActionCommand())) {
            cardName = "CardProgress";
            buttonExecute.setText("Stop collector");
        } else if("Stop collector".equals(e.getActionCommand())) {
            cardName = "CardOverview";
            buttonExecute.setText("Start collector");
        } else if("Schedule collector".equals(e.getActionCommand())) {
            // no layout change
        }
        cardLayout.show(panelCardLayout, cardName);
    }

    public JPanel getPanel() {
        return panel;
    }

    public JButton getButton() {
        return buttonExecute;
    }

    @Override
    public void update(Observable o, Object arg) {
        buttonExecute.setEnabled(!arg.equals(OverviewPanel.Status.ERROR));
    }

    public void setSchedule(boolean enabled) {
        buttonExecute.setText(enabled ? "Schedule collector" : "Start collector");
    }
}
