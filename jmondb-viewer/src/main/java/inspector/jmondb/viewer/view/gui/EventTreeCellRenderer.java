package inspector.jmondb.viewer.view.gui;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class EventTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final ImageIcon ICON_EVENT = new ImageIcon(EventTreeCellRenderer.class.getResource("/images/event.png"));
    private static final ImageIcon ICON_UNDEFINED = new ImageIcon(EventTreeCellRenderer.class.getResource("/images/undefined.png"));
    private static final ImageIcon ICON_CALIBRATION = new ImageIcon(EventTreeCellRenderer.class.getResource("/images/calibration.png"));
    private static final ImageIcon ICON_MAINTENANCE = new ImageIcon(EventTreeCellRenderer.class.getResource("/images/maintenance.png"));
    private static final ImageIcon ICON_INCIDENT = new ImageIcon(EventTreeCellRenderer.class.getResource("/images/incident.png"));

    public EventTreeCellRenderer() {
        super();
    }

    //TODO: retrieve the colors from a settings class
    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value, boolean selected, boolean expanded,
                                                  boolean isLeaf, int row, boolean focused) {
        // because of some weird bug the text color has to be set before calling super
        // and the icons have to be set after calling super
        String s = value.toString();
        switch(s) {
            case "Undefined":
                setTextNonSelectionColor(Color.ORANGE);
                setTextSelectionColor(Color.ORANGE);
                break;
            case "Calibration":
                setTextNonSelectionColor(Color.GREEN);
                setTextSelectionColor(Color.GREEN);
                break;
            case "Maintenance":
                setTextNonSelectionColor(Color.BLUE);
                setTextSelectionColor(Color.BLUE);
                break;
            case "Incident":
                setTextNonSelectionColor(Color.RED);
                setTextSelectionColor(Color.RED);
                break;
            default:
                setTextNonSelectionColor(Color.BLACK);
                setTextSelectionColor(Color.WHITE);
                break;
        }

        super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);

        switch(s) {
            case "Events":
                setIcon(ICON_EVENT);
                setClosedIcon(ICON_EVENT);
                setOpenIcon(ICON_EVENT);
                break;
            case "Undefined":
                setIcon(ICON_UNDEFINED);
                setClosedIcon(ICON_UNDEFINED);
                setOpenIcon(ICON_UNDEFINED);
                break;
            case "Calibration":
                setIcon(ICON_CALIBRATION);
                setClosedIcon(ICON_CALIBRATION);
                setOpenIcon(ICON_CALIBRATION);
                break;
            case "Maintenance":
                setIcon(ICON_MAINTENANCE);
                setClosedIcon(ICON_MAINTENANCE);
                setOpenIcon(ICON_MAINTENANCE);
                break;
            case "Incident":
                setIcon(ICON_INCIDENT);
                setClosedIcon(ICON_INCIDENT);
                setOpenIcon(ICON_INCIDENT);
                break;
            default:
                break;
        }

        return this;
    }
}
