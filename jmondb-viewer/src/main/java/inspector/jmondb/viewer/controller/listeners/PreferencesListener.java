package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.model.EventType;
import inspector.jmondb.viewer.model.VisualizationConfiguration;
import inspector.jmondb.viewer.view.gui.ConfigTabbedPane;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PreferencesListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private VisualizationConfiguration configuration;

    public PreferencesListener(ViewerFrame viewerFrame, VisualizationConfiguration configuration) {
        this.viewerFrame = viewerFrame;

        this.configuration = configuration;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigTabbedPane configPane = new ConfigTabbedPane(viewerFrame, configuration);

        String[] options = new String[] { "OK", "Cancel", "Reset" };
        int option = JOptionPane.showOptionDialog(viewerFrame.getFrame(), configPane.getTabbedPane(),
                "Preferences", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if(option == JOptionPane.OK_OPTION) {
            // update preferences
            for(EventType type : EventType.values()) {
                configuration.setColor(type, configPane.getVisualizationConfigPanel().getColor(type));
            }

            refresh();
        } else if(option == JOptionPane.CANCEL_OPTION) {
            // reset preferences
            configuration.reset();

            refresh();
        }
    }

    private void refresh() {
        // refresh visualizations
        viewerFrame.getEventPanel().getEventTree().refreshColor();
        viewerFrame.getGraphPanel().getEventMarkers().refreshColor();
    }
}
