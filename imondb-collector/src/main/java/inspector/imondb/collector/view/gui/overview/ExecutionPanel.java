package inspector.imondb.collector.view.gui.overview;

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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import inspector.imondb.collector.controller.ExecutionController;
import inspector.imondb.collector.view.gui.CollectorFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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

        buttonExecute.addActionListener(e -> {
            // change the card layout
            updateLayout(e);

            // execute the button functionality
            if("Start collector".equals(e.getActionCommand())) {
                progressPanel.start();
            } else if("Stop collector".equals(e.getActionCommand())) {
                progressPanel.stop();
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow", "center:d:grow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
        panel.add(panel1);
        panelCardLayout = new JPanel();
        panelCardLayout.setLayout(new CardLayout(0, 0));
        CellConstraints cc = new CellConstraints();
        panel1.add(panelCardLayout, cc.xy(1, 1));
        panelOverview = new JPanel();
        panelOverview.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panelCardLayout.add(panelOverview, "CardOverview");
        panelProgress = new JPanel();
        panelProgress.setLayout(new BorderLayout(0, 0));
        panelCardLayout.add(panelProgress, "CardProgress");
        buttonExecute = new JButton();
        buttonExecute.setText("Start collector");
        buttonExecute.setMnemonic('S');
        buttonExecute.setDisplayedMnemonicIndex(0);
        panel1.add(buttonExecute, cc.xy(1, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}
