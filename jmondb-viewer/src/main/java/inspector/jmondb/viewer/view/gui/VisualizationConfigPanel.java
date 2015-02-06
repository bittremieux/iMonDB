package inspector.jmondb.viewer.view.gui;

import com.bric.swing.ColorPicker;
import com.bric.swing.ColorWell;
import inspector.jmondb.model.EventType;
import inspector.jmondb.viewer.model.VisualizationConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class VisualizationConfigPanel {

    private ViewerFrame viewerFrame;

    private JPanel panel;

    private ColorWell colorUndefined;
    private ColorWell colorCalibration;
    private ColorWell colorMaintenance;
    private ColorWell colorIncident;

    public VisualizationConfigPanel(ViewerFrame viewerFrame, VisualizationConfiguration configuration) {
        this.viewerFrame = viewerFrame;

        panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Specify the color of the various event types.");
        panel.add(title, BorderLayout.PAGE_START);
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));

        JPanel colorPanel = new JPanel(new GridLayout(0, 2, 0, 5));

        colorPanel.add(new JLabel("Undefined:"));
        colorUndefined = createColorWell(configuration.getColor(EventType.UNDEFINED));
        colorPanel.add(colorUndefined);

        colorPanel.add(new JLabel("Calibration:"));
        colorCalibration = createColorWell(configuration.getColor(EventType.CALIBRATION));
        colorPanel.add(colorCalibration);

        colorPanel.add(new JLabel("Maintenance:"));
        colorMaintenance = createColorWell(configuration.getColor(EventType.MAINTENANCE));
        colorPanel.add(colorMaintenance);

        colorPanel.add(new JLabel("Incident:"));
        colorIncident = createColorWell(configuration.getColor(EventType.INCIDENT));
        colorPanel.add(colorIncident);

        panel.add(colorPanel, BorderLayout.CENTER);
    }

    public JPanel getPanel() {
        return panel;
    }

    public Color getColor(EventType type) {
        switch(type) {
            case UNDEFINED:
                return colorUndefined.getColor();
            case CALIBRATION:
                return colorCalibration.getColor();
            case MAINTENANCE:
                return colorMaintenance.getColor();
            case INCIDENT:
                return colorIncident.getColor();
            default:
                return Color.BLACK;
        }
    }

    private ColorWell createColorWell(Color color) {
        ColorWell colorWell = new ColorWell(color);
        // remove default mouse listener
        for(MouseListener listener : colorWell.getMouseListeners()) {
            colorWell.removeMouseListener(listener);
        }
        colorWell.addMouseListener(new ColorWellMouseListener());

        return colorWell;
    }

    private class ColorWellMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            ColorWell source = (ColorWell) e.getSource();
            Color color = ColorPicker.showDialog(viewerFrame.getFrame(), source.getColor());
            source.setColor(color != null ? color : source.getColor());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // do nothing
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // do nothing
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // do nothing
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // do nothing
        }
    }
}

