package inspector.imondb.collector.view.gui.metadata;

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

import inspector.imondb.collector.model.MetadataMap;

import javax.swing.*;
import java.awt.*;

public class MetadataOverviewPanel {

    private JPanel panel;

    private JLabel labelName;
    private JLabel labelSource;
    private JLabel labelRegex;

    private JButton buttonEdit;
    private JButton buttonRemove;

    private MetadataMap metadataMap;

    private MetadataOverviewPanel(MetadataPanel metadataPanel) {
        buttonEdit.addActionListener(e -> {
            // edit
            MetadataCreatePanel metadataCreatePanel = new MetadataCreatePanel(metadataMap);
            int result = JOptionPane.showConfirmDialog(Frame.getFrames()[0], metadataCreatePanel.getPanel(),
                    "Edit metadata", JOptionPane.OK_CANCEL_OPTION);

            if(result == JOptionPane.OK_OPTION) {
                MetadataMap metadataMapNew = metadataCreatePanel.getMetadataMap();
                if(metadataMapNew.isValid()) {
                    setMetadataMap(metadataMapNew);
                    metadataPanel.editMetadata(metadataMapNew);
                } else {
                    JOptionPane.showMessageDialog(Frame.getFrames()[0],
                            "<html>Invalid metadata configuration.<br><br>Your changes were undone.</html>",
                            "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        buttonRemove.addActionListener(e -> {
            metadataPanel.removeMetadata(metadataMap);

            metadataPanel.getPanel().revalidate();
            metadataPanel.getPanel().repaint();
        });
    }

    public MetadataOverviewPanel(MetadataPanel metadataPanel, MetadataMap metadataMap) {
        this(metadataPanel);

        setMetadataMap(metadataMap);
    }

    private void setMetadataMap(MetadataMap metadataMap) {
        this.metadataMap = metadataMap;
        labelName.setText(metadataMap.getKey() + " = " + metadataMap.getValue());
        labelSource.setText(metadataMap.getSource().toString());
        labelRegex.setText(metadataMap.getRegex());
    }

    public JPanel getPanel() {
        return panel;
    }
}
