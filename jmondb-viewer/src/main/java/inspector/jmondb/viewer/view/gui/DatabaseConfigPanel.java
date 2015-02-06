package inspector.jmondb.viewer.view.gui;

import inspector.jmondb.viewer.model.Configuration;
import inspector.jmondb.viewer.model.DatabaseConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class DatabaseConfigPanel extends Configuration {

    private JPanel panel;

    private JCheckBox checkBox;

    private DatabaseConnectionPanel databaseConnectionPanel;

    public DatabaseConfigPanel(DatabaseConfiguration configuration) {
        panel = new JPanel(new BorderLayout());

        checkBox = new JCheckBox("Automatically connect to the database on application startup");
        checkBox.setSelected(configuration.getAutoConnect());
        checkBox.addItemListener(e -> databaseConnectionPanel.setEnabled(e.getStateChange() == ItemEvent.SELECTED));
        panel.add(checkBox, BorderLayout.PAGE_START);
        checkBox.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));

        databaseConnectionPanel = new DatabaseConnectionPanel(configuration.getHost(), configuration.getPort(),
                configuration.getUserName(), configuration.getPassword(), configuration.getDatabase());
        databaseConnectionPanel.setEnabled(getAutoConnect());
        panel.add(databaseConnectionPanel.getPanel(), BorderLayout.CENTER);
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean getAutoConnect() {
        return checkBox.isSelected();
    }

    public DatabaseConnectionPanel getDatabaseConnectionPanel() {
        return databaseConnectionPanel;
    }
}
