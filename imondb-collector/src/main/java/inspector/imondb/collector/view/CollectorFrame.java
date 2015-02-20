package inspector.imondb.collector.view;

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

import inspector.imondb.collector.controller.CollectorController;
import inspector.imondb.collector.controller.ExecutionController;
import inspector.imondb.collector.controller.listeners.ConfigurationChangeListener;
import inspector.imondb.collector.controller.listeners.DatabaseConnectionListener;
import inspector.imondb.collector.model.config.Configuration;
import inspector.imondb.collector.model.config.DatabaseConfiguration;
import inspector.imondb.collector.model.config.GeneralConfiguration;
import inspector.imondb.collector.view.database.DatabasePanel;
import inspector.imondb.collector.view.general.GeneralPanel;
import inspector.imondb.collector.view.instrument.InstrumentsPanel;
import inspector.imondb.collector.view.metadata.MetadataPanel;
import inspector.imondb.collector.view.overview.ExecutionPanel;
import inspector.imondb.collector.view.overview.OverviewPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CollectorFrame {

    private CollectorController collectorController;

    private JFrame frame;

    // menu items and buttons to connect ActionListeners to
    private JMenuItem menuItemExit;
    private JMenuItem menuItemUpdate;
    private JMenuItem menuItemAbout;

    // panel containers
    private JTabbedPane tabbedPane;

    private DatabasePanel databasePanel;
    private GeneralPanel generalPanel;
    private InstrumentsPanel instrumentsPanel;
    private MetadataPanel metadataPanel;
    private OverviewPanel overviewPanel;

    public CollectorFrame(CollectorController collectorController, ExecutionController executionController, Configuration configuration) {
        this.collectorController = collectorController;

        frame = new JFrame("iMonDB Collector");
        frame.setIconImage(new ImageIcon(getClass().getResource("/images/logo-small.png")).getImage());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panelParent = new JPanel(new BorderLayout());
        frame.setContentPane(panelParent);

        // create menu bar
        frame.setJMenuBar(createMenuBar());

        // create and arrange panels
        tabbedPane = new JTabbedPane();

        DatabaseConfiguration databaseConfiguration = configuration.getDatabaseConfiguration();
        databasePanel = new DatabasePanel(databaseConfiguration.getHost(), databaseConfiguration.getPort(),
                databaseConfiguration.getUserName(), databaseConfiguration.getPassword(), databaseConfiguration.getDatabase());
        tabbedPane.addTab("Database", databasePanel.getPanel());

        GeneralConfiguration generalConfiguration = configuration.getGeneralConfiguration();
        generalPanel = new GeneralPanel(generalConfiguration.getDirectory(), generalConfiguration.getFileNameRegex(),
                generalConfiguration.getStartDate(), generalConfiguration.getNumberOfThreads(), generalConfiguration.getUniqueFileNames());
        tabbedPane.addTab("General", generalPanel.getPanel());

        instrumentsPanel = new InstrumentsPanel(configuration.getInstrumentConfiguration().getInstruments());
        tabbedPane.addTab("Instruments", instrumentsPanel.getPanel());

        metadataPanel = new MetadataPanel(configuration.getMetadataConfiguration().getMetadata());
        tabbedPane.addTab("Metadata", metadataPanel.getPanel());

        overviewPanel = new OverviewPanel(this);
        ExecutionPanel executionPanel = new ExecutionPanel(this, executionController);
        tabbedPane.addTab("Execute", executionPanel.getPanel());

        tabbedPane.addChangeListener(e -> {
            if("Execute".equals(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()))) {
                overviewPanel.update();
            }
        });

        frame.setContentPane(tabbedPane);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // file menu
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);

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

    public void initialize() {
        // check for updates on startup
        for(ActionListener a : menuItemUpdate.getActionListeners()) {
            a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "auto update"));
        }

        // set the status overview
        overviewPanel.update();
    }

    public void display() {
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    public void setWaitCursor(boolean wait) {
        frame.setCursor(wait ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    public DatabasePanel getDatabasePanel() {
        return databasePanel;
    }

    public GeneralPanel getGeneralPanel() {
        return  generalPanel;
    }

    public InstrumentsPanel getInstrumentsPanel() {
        return instrumentsPanel;
    }

    public MetadataPanel getMetadataPanel() {
        return metadataPanel;
    }

    public OverviewPanel getOverviewPanel() {
        return overviewPanel;
    }

    public void addExitAction(Action action) {
        menuItemExit.setAction(action);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                collectorController.cleanUp();
            }
        });
    }

    public void addAboutDisplayer(ActionListener listener) {
        menuItemAbout.addActionListener(listener);
    }

    public void addUpdateChecker(ActionListener listener) {
        menuItemUpdate.addActionListener(listener);
    }

    public void addConfigurationChangeListener(ConfigurationChangeListener listener) {
        databasePanel.addConfigurationChangeListener(listener);
        generalPanel.addConfigurationChangeListener(listener);
        instrumentsPanel.addConfigurationChangeListener(listener);
        metadataPanel.addConfigurationChangeListener(listener);
    }

    public void addDatabaseConnectionListener(DatabaseConnectionListener databaseConnectionListener) {
        databasePanel.addDatabaseConnectionListener(databaseConnectionListener);
        instrumentsPanel.addObserver(databaseConnectionListener);
    }
}
