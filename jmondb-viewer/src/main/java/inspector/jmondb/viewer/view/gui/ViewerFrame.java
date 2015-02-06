package inspector.jmondb.viewer.view.gui;

/*
 * #%L
 * jMonDB Viewer
 * %%
 * Copyright (C) 2014 InSPECtor
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

import inspector.jmondb.viewer.model.DatabaseConfiguration;
import inspector.jmondb.viewer.model.VisualizationConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ViewerFrame {

    private DatabaseConfiguration databaseConfiguration;

    private JFrame frame;

    // menu items and buttons to connect ActionListeners to
    private JMenuItem menuItemConnectToDb;
    private JMenuItem menuItemDisconnectFromDb;
    private JMenuItem menuItemSaveGraph;
    private JMenuItem menuItemExportEvents;
    private JMenuItem menuItemPreferences;
    private JMenuItem menuItemExit;
    private JMenuItem menuItemUpdate;
    private JMenuItem menuItemAbout;

    private JButton buttonConnectToDatabase;

    // editable panels
    private DatabasePanel panelDatabase;
    private PropertySelectionPanel propertySelectionPanel;
    private GraphPanel panelGraph;
    private EventPanel panelEvent;

    public ViewerFrame(VisualizationConfiguration configuration, DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;

        frame = new JFrame("iMonDB Viewer");
        frame.setIconImage(new ImageIcon(getClass().getResource("/images/logo-small.png")).getImage());

        JPanel panelParent = new JPanel(new BorderLayout());
        frame.setContentPane(panelParent);

        // create menu bar
        frame.setJMenuBar(createMenuBar());

        // create and arrange panels
        panelGraph = new GraphPanel(configuration);
        panelDatabase = new DatabasePanel();
        panelEvent = new EventPanel(configuration);
        panelParent.add(panelGraph.getPanel(), BorderLayout.CENTER);
        panelParent.add(createTopPanel(), BorderLayout.PAGE_START);
        panelParent.add(panelDatabase.getPanel(), BorderLayout.PAGE_END);
        panelParent.add(panelEvent.getPanel(), BorderLayout.LINE_END);

        panelDatabase.getPanel().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 0, 0, 0),
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY)));
        panelEvent.getPanel().setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
    }

    public void initialize() {
        // check for updates on startup
        for(ActionListener a : menuItemUpdate.getActionListeners()) {
            a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Initialization update check"));
        }

        // auto connect to a database
        if(databaseConfiguration.getAutoConnect()) {
            for(ActionListener a : buttonConnectToDatabase.getActionListeners()) {
                a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Auto connect"));
            }
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public DatabasePanel getDatabasePanel() {
        return panelDatabase;
    }

    public PropertySelectionPanel getPropertySelectionPanel() {
        return propertySelectionPanel;
    }

    public GraphPanel getGraphPanel() {
        return panelGraph;
    }

    public EventPanel getEventPanel() {
        return panelEvent;
    }

    public void display() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1280, 800));
        frame.setPreferredSize(new Dimension(1280, 800));
        frame.pack();
        frame.setVisible(true);
    }

    public void setWaitCursor(boolean wait) {
        if(wait) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            frame.setCursor(Cursor.getDefaultCursor());
        }
    }

    public void addExitAction(Action action) {
        menuItemExit.setAction(action);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                action.actionPerformed(new ActionEvent(e.getSource(), e.getID(), "Exit"));
            }
        });
    }

    public void addAboutDisplayer(ActionListener listener) {
        menuItemAbout.addActionListener(listener);
    }

    public void addDatabaseConnector(ActionListener listener) {
        menuItemConnectToDb.addActionListener(listener);
        buttonConnectToDatabase.addActionListener(listener);
    }

    public void addDatabaseDisconnector(ActionListener listener) {
        menuItemDisconnectFromDb.addActionListener(listener);
    }

    public void addGraphDisplayer(ActionListener listener) {
        propertySelectionPanel.addPropertyChangeListener(listener);
        panelGraph.addGraphAdvancer(listener);
    }

    public void addGraphSaver(ActionListener listener) {
        menuItemSaveGraph.addActionListener(listener);
    }

    public void addInstrumentChangeListener(ActionListener listener) {
        propertySelectionPanel.addInstrumentChangeListener(listener);
    }

    public void addPropertyChangeListener(ActionListener listener) {
        propertySelectionPanel.addPropertyChangeListener(listener);
    }

    public void addEventCheckBoxListener(ItemListener listener) {
        panelEvent.getEventConfigurationPanel().addCheckBoxListener(listener);
    }

    public void addEventCreator(ActionListener listener) {
        panelEvent.getButtonAdd().addActionListener(listener);
    }

    public void addEventRemover(ActionListener listener) {
        panelEvent.getButtonRemove().addActionListener(listener);
        panelEvent.getEventTree().setEventRemoveListener(listener);
    }

    public void addEventEditor(ActionListener listener) {
        panelEvent.getEventTree().setEventEditListener(listener);
    }

    public void addEventClearer(ActionListener listener) {
        panelEvent.getButtonClear().addActionListener(listener);
    }

    public void addEventExporter(ActionListener listener) {
        panelEvent.getButtonExport().addActionListener(listener);
        menuItemExportEvents.addActionListener(listener);
    }

    public void addAdvancedSearchDisplayer(ActionListener listener) {
        propertySelectionPanel.addAdvancedSearchListener(listener);
    }

    public void addUpdateChecker(ActionListener listener) {
        menuItemUpdate.addActionListener(listener);
    }

    public void addPreferencesDisplayer(ActionListener listener) {
        menuItemPreferences.addActionListener(listener);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // file menu
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);
        menuItemConnectToDb = new JMenuItem("Connect to the database");
        menuItemConnectToDb.setMnemonic(KeyEvent.VK_C);
        menuFile.add(menuItemConnectToDb);
        menuItemDisconnectFromDb = new JMenuItem("Disconnect from the database");
        menuItemDisconnectFromDb.setMnemonic(KeyEvent.VK_D);
        menuFile.add(menuItemDisconnectFromDb);

        menuFile.addSeparator();

        menuItemSaveGraph = new JMenuItem("Save graph as...");
        menuItemSaveGraph.setMnemonic(KeyEvent.VK_S);
        menuItemSaveGraph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuFile.add(menuItemSaveGraph);
        menuItemExportEvents = new JMenuItem("Export event log as...");
        menuItemExportEvents.setMnemonic(KeyEvent.VK_E);
        menuItemExportEvents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuFile.add(menuItemExportEvents);

        menuFile.addSeparator();

        menuItemPreferences = new JMenuItem("Preferences");
        menuItemPreferences.setMnemonic(KeyEvent.VK_I);
        menuItemPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuFile.add(menuItemPreferences);

        menuFile.addSeparator();

        menuItemExit = new JMenuItem("Exit");
        menuItemExit.setMnemonic(KeyEvent.VK_X);
        menuFile.add(menuItemExit);

        // add to menu bar
        menuBar.add(menuFile);

        // help menu
        JMenu menuHelp = new JMenu("Help");
        menuItemUpdate = new JMenuItem("Check for updates");
        menuItemUpdate.setMnemonic(KeyEvent.VK_U);
        menuHelp.add(menuItemUpdate);
        menuHelp.setMnemonic(KeyEvent.VK_H);
        menuItemAbout = new JMenuItem("About");
        menuItemAbout.setMnemonic(KeyEvent.VK_A);
        menuHelp.add(menuItemAbout);

        // add to menu bar
        menuBar.add(menuHelp);

        return menuBar;
    }

    private JPanel createTopPanel() {
        JPanel panelTop = new JPanel();

        buttonConnectToDatabase = new JButton("Connect to database");
        panelTop.add(buttonConnectToDatabase);

        propertySelectionPanel = new PropertySelectionPanel();
        panelTop.add(propertySelectionPanel.getPanel());

        return panelTop;
    }
}
