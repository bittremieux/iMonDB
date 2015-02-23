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

import inspector.imondb.collector.controller.listeners.ConfigurationChangeListener;
import inspector.imondb.collector.model.InstrumentMap;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class InstrumentsPanel extends Observable {

    private JPanel panel;

    private JButton buttonAdd;
    private JButton buttonTest;

    private JScrollPane scrollPaneInstruments;
    private JPanel panelInstruments;
    private Map<InstrumentMap, InstrumentOverviewPanel> instrumentMaps;

    public InstrumentsPanel() {
        instrumentMaps = new HashMap<>();

        $$$setupUI$$$();
        buttonAdd.addActionListener(e -> {
            InstrumentCreatePanel instrumentCreatePanel = new InstrumentCreatePanel();
            int result = JOptionPane.showConfirmDialog(Frame.getFrames()[0], instrumentCreatePanel.getPanel(),
                    "Create instrument", JOptionPane.OK_CANCEL_OPTION);
            if(result == JOptionPane.OK_OPTION) {
                InstrumentMap instrumentMap = instrumentCreatePanel.getInstrumentMap();
                if(instrumentMap.isValid() && !instrumentMaps.containsKey(instrumentMap)) {
                    addInstrument(instrumentMap);
                    panelInstruments.revalidate();
                    panelInstruments.repaint();
                    scrollPaneInstruments.revalidate();
                } else {
                    JOptionPane.showMessageDialog(Frame.getFrames()[0], "<html>Invalid instrument configuration.<br><br>Please try to add the instrument again.<br>" +
                                    "Make sure that all fields are correctly set,<br>and that no other instrument with the<br>same name already exists.</html>",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonTest.addActionListener(e -> {
            RegexMapTestPanel regexMapTestPanel = new RegexMapTestPanel(getInstruments());
            JOptionPane.showMessageDialog(panel, regexMapTestPanel.getPanel(), "Test instrument configuration", JOptionPane.PLAIN_MESSAGE);
        });
    }

    public InstrumentsPanel(Collection<InstrumentMap> instruments) {
        this();

        instruments.forEach(this::addInstrument);
    }

    private void createUIComponents() {
        panelInstruments = new JPanel();
        panelInstruments.setLayout(new BoxLayout(panelInstruments, BoxLayout.PAGE_AXIS));
    }

    public JPanel getPanel() {
        return panel;
    }

    private void addInstrument(InstrumentMap instrumentMap) {
        InstrumentOverviewPanel instrumentOverviewPanel = new InstrumentOverviewPanel(this, instrumentMap);
        instrumentOverviewPanel.getPanel().setAlignmentX(Component.LEFT_ALIGNMENT);
        panelInstruments.add(instrumentOverviewPanel.getPanel());

        instrumentMaps.put(instrumentMap, instrumentOverviewPanel);

        setChanged();
        notifyObservers(instrumentMap);
    }

    void removeInstrument(InstrumentMap instrumentMap) {
        panelInstruments.remove(instrumentMaps.get(instrumentMap).getPanel());
        instrumentMaps.remove(instrumentMap);

        setChanged();
        notifyObservers();
    }

    void editInstrument(InstrumentMap instrumentMap) {
        InstrumentOverviewPanel instrumentOverviewPanel = instrumentMaps.remove(instrumentMap);
        instrumentMaps.put(instrumentMap, instrumentOverviewPanel);

        setChanged();
        notifyObservers(instrumentMap);
    }

    public Collection<InstrumentMap> getInstruments() {
        return instrumentMaps.keySet();
    }

    public InstrumentOverviewPanel.InstrumentStatus getInstrumentStatus(InstrumentMap instrumentMap) {
        return instrumentMaps.get(instrumentMap).getStatus();
    }

    public void setInstrumentStatus(InstrumentMap instrumentMap, InstrumentOverviewPanel.InstrumentStatus status) {
        if(instrumentMaps.containsKey(instrumentMap)) {
            InstrumentOverviewPanel instrumentOverviewPanel = instrumentMaps.get(instrumentMap);
            switch(status) {
                case VALID:
                    instrumentOverviewPanel.setStatus(InstrumentOverviewPanel.InstrumentStatus.VALID);
                    break;
                case INVALID:
                    instrumentOverviewPanel.setStatus(InstrumentOverviewPanel.InstrumentStatus.INVALID);
                    break;
                case NEW:
                case UNKNOWN:
                default:
                    instrumentOverviewPanel.setStatus(InstrumentOverviewPanel.InstrumentStatus.UNKNOWN);
                    break;
            }
        }
    }

    public void addConfigurationChangeListener(ConfigurationChangeListener listener) {
        addObserver(listener);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setMinimumSize(new Dimension(500, 250));
        panel1.setPreferredSize(new Dimension(500, 250));
        panel.add(panel1);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JLabel label1 = new JLabel();
        label1.setText("<html><b>Instruments</b><html>");
        label1.setDisplayedMnemonic('I');
        label1.setDisplayedMnemonicIndex(9);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label1, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel2.setMinimumSize(new Dimension(405, 200));
        panel2.setPreferredSize(new Dimension(405, 200));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        scrollPaneInstruments = new JScrollPane();
        scrollPaneInstruments.setHorizontalScrollBarPolicy(31);
        scrollPaneInstruments.setVerticalScrollBarPolicy(20);
        panel2.add(scrollPaneInstruments, BorderLayout.CENTER);
        scrollPaneInstruments.setViewportView(panelInstruments);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.TRAILING, 5, 5));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        buttonAdd = new JButton();
        buttonAdd.setHorizontalAlignment(0);
        buttonAdd.setHorizontalTextPosition(0);
        buttonAdd.setIcon(new ImageIcon(getClass().getResource("/images/add.png")));
        buttonAdd.setMaximumSize(new Dimension(29, 29));
        buttonAdd.setMinimumSize(new Dimension(29, 29));
        buttonAdd.setPreferredSize(new Dimension(29, 29));
        buttonAdd.setText("");
        buttonAdd.setToolTipText("Add a new instrument");
        panel3.add(buttonAdd);
        buttonTest = new JButton();
        buttonTest.setHorizontalTextPosition(0);
        buttonTest.setIcon(new ImageIcon(getClass().getResource("/images/search.png")));
        buttonTest.setMaximumSize(new Dimension(29, 29));
        buttonTest.setMinimumSize(new Dimension(29, 29));
        buttonTest.setPreferredSize(new Dimension(29, 29));
        buttonTest.setRolloverEnabled(true);
        buttonTest.setText("");
        buttonTest.setToolTipText("Test the instrument regex configuration");
        panel3.add(buttonTest);
        label1.setLabelFor(scrollPaneInstruments);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}
