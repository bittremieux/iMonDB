package inspector.imondb.collector.view.gui.instrument;

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

import inspector.imondb.collector.model.InstrumentMap;

import javax.swing.*;
import java.awt.*;

public class InstrumentOverviewPanel {

    public enum InstrumentStatus { VALID, NEW, UNKNOWN, INVALID }

    private static ImageIcon iconInvalid = new ImageIcon(InstrumentOverviewPanel.class.getResource("/images/nok.png"));
    private static ImageIcon iconWarning = new ImageIcon(InstrumentOverviewPanel.class.getResource("/images/warning.png"));
    private static ImageIcon iconValid = new ImageIcon(InstrumentOverviewPanel.class.getResource("/images/ok.png"));

    private JPanel panel;

    private JLabel labelName;

    private JLabel labelStatus;
    private JLabel labelSource;
    private JLabel labelRegex;

    private JButton buttonEdit;
    private JButton buttonRemove;

    private InstrumentMap instrumentMap;
    private InstrumentStatus status;

    private InstrumentOverviewPanel(InstrumentsPanel instrumentsPanel) {
        status = InstrumentStatus.UNKNOWN;

        buttonEdit.addActionListener(e -> {
            // edit
            InstrumentCreatePanel instrumentCreatePanel = new InstrumentCreatePanel(instrumentMap);

            int result = JOptionPane.showConfirmDialog(Frame.getFrames()[0], instrumentCreatePanel.getPanel(),
                    "Edit instrument", JOptionPane.OK_CANCEL_OPTION);

            if(result == JOptionPane.OK_OPTION) {
                InstrumentMap instrumentMapNew = instrumentCreatePanel.getInstrumentMap();
                if(instrumentMapNew.isValid()) {
                    setInstrumentMap(instrumentMapNew);
                    instrumentsPanel.editInstrument(instrumentMapNew);
                } else {
                    JOptionPane.showMessageDialog(Frame.getFrames()[0],
                            "<html>Invalid instrument configuration.<br><br>Your changes were undone.</html>",
                            "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        buttonRemove.addActionListener(e -> {
            instrumentsPanel.removeInstrument(instrumentMap);

            instrumentsPanel.getPanel().revalidate();
            instrumentsPanel.getPanel().repaint();
        });
    }

    public InstrumentOverviewPanel(InstrumentsPanel instrumentsPanel, InstrumentMap instrumentMap) {
        this(instrumentsPanel);

        setInstrumentMap(instrumentMap);
    }

    private void setInstrumentMap(InstrumentMap instrumentMap) {
        this.instrumentMap = instrumentMap;
        labelName.setText(instrumentMap.getKey() + " (" + instrumentMap.getValue().toString() + ")");
        labelSource.setText(instrumentMap.getSource().toString());
        labelRegex.setText(instrumentMap.getRegex());
    }

    public JPanel getPanel() {
        return panel;
    }

    public InstrumentStatus getStatus() {
        return status;
    }

    void setStatus(InstrumentStatus status) {
        this.status = status;

        switch(status) {
            case VALID:
                labelStatus.setIcon(iconValid);
                labelStatus.setToolTipText("Valid instrument");
                break;
            case INVALID:
                labelStatus.setIcon(iconInvalid);
                labelStatus.setToolTipText("Instrument definition conflicting with the database information");
                break;
            case UNKNOWN:
            case NEW:
            default:
                labelStatus.setIcon(iconWarning);
                labelStatus.setToolTipText("Unverified/new instrument");
                break;
        }
    }
}
