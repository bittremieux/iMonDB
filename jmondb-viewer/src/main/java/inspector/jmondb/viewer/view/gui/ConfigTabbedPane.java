package inspector.jmondb.viewer.view.gui;

import inspector.jmondb.viewer.model.DatabaseConfiguration;
import inspector.jmondb.viewer.model.VisualizationConfiguration;

import javax.swing.*;

public class ConfigTabbedPane {

    private JTabbedPane tabbedPane;

    private VisualizationConfigPanel visualizationConfigPanel;
    private DatabaseConfigPanel databaseConfigPanel;

    public ConfigTabbedPane(ViewerFrame viewerFrame, VisualizationConfiguration visualizationConfiguration,
                            DatabaseConfiguration databaseConfiguration) {
        tabbedPane = new JTabbedPane();

        visualizationConfigPanel = new VisualizationConfigPanel(viewerFrame, visualizationConfiguration);
        JPanel visualizationPanel = visualizationConfigPanel.getPanel();
        tabbedPane.addTab("Visualization", visualizationPanel);
        visualizationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        databaseConfigPanel = new DatabaseConfigPanel(databaseConfiguration);
        JPanel databasePanel = databaseConfigPanel.getPanel();
        tabbedPane.addTab("Database", databaseConfigPanel.getPanel());
        databasePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public VisualizationConfigPanel getVisualizationConfigPanel() {
        return visualizationConfigPanel;
    }

    public DatabaseConfigPanel getDatabaseConfigPanel() {
        return databaseConfigPanel;
    }
}
