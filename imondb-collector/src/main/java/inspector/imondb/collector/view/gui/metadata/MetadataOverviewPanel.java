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
        panel.setLayout(new GridBagLayout());
        panel.setMaximumSize(new Dimension(380, 40));
        panel.setMinimumSize(new Dimension(380, 40));
        panel.setPreferredSize(new Dimension(380, 40));
        labelName = new JLabel();
        labelName.setMaximumSize(new Dimension(305, 16));
        labelName.setMinimumSize(new Dimension(305, 16));
        labelName.setPreferredSize(new Dimension(305, 16));
        labelName.setText("Key = Value");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(labelName, gbc);
        buttonEdit = new JButton();
        buttonEdit.setActionCommand("Edit");
        buttonEdit.setHorizontalTextPosition(0);
        buttonEdit.setIcon(new ImageIcon(getClass().getResource("/images/edit.png")));
        buttonEdit.setLabel("");
        buttonEdit.setMaximumSize(new Dimension(29, 29));
        buttonEdit.setMinimumSize(new Dimension(29, 29));
        buttonEdit.setPreferredSize(new Dimension(29, 29));
        buttonEdit.setText("");
        buttonEdit.setToolTipText("Edit the instrument");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(buttonEdit, gbc);
        buttonRemove = new JButton();
        buttonRemove.setActionCommand("Delete");
        buttonRemove.setHideActionText(true);
        buttonRemove.setHorizontalTextPosition(0);
        buttonRemove.setIcon(new ImageIcon(getClass().getResource("/images/delete.png")));
        buttonRemove.setLabel("");
        buttonRemove.setMaximumSize(new Dimension(29, 29));
        buttonRemove.setMinimumSize(new Dimension(29, 29));
        buttonRemove.setPreferredSize(new Dimension(29, 29));
        buttonRemove.setText("");
        buttonRemove.setToolTipText("Delete the instrument");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(buttonRemove, gbc);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(panel1, gbc);
        labelSource = new JLabel();
        labelSource.setMaximumSize(new Dimension(65, 16));
        labelSource.setMinimumSize(new Dimension(65, 16));
        labelSource.setPreferredSize(new Dimension(65, 16));
        labelSource.setText("source");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(labelSource, gbc);
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setMaximumSize(new Dimension(30, 16));
        label1.setMinimumSize(new Dimension(30, 16));
        label1.setPreferredSize(new Dimension(30, 16));
        label1.setText("->");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label1, gbc);
        labelRegex = new JLabel();
        labelRegex.setMaximumSize(new Dimension(170, 16));
        labelRegex.setMinimumSize(new Dimension(170, 16));
        labelRegex.setPreferredSize(new Dimension(170, 16));
        labelRegex.setText("regex");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(labelRegex, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer3, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}