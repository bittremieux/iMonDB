package inspector.imondb.viewer.view.gui;

/*
 * #%L
 * iMonDB Viewer
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

import inspector.imondb.model.EventType;
import inspector.imondb.viewer.model.VisualizationConfiguration;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class EventTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final ImageIcon ICON_EVENT = new ImageIcon(EventTreeCellRenderer.class.getResource("/images/event.png"));
    private static final ImageIcon ICON_UNDEFINED = new ImageIcon(EventTreeCellRenderer.class.getResource("/images/undefined.png"));
    private static final ImageIcon ICON_CALIBRATION = new ImageIcon(EventTreeCellRenderer.class.getResource("/images/calibration.png"));
    private static final ImageIcon ICON_MAINTENANCE = new ImageIcon(EventTreeCellRenderer.class.getResource("/images/maintenance.png"));
    private static final ImageIcon ICON_INCIDENT = new ImageIcon(EventTreeCellRenderer.class.getResource("/images/incident.png"));

    private VisualizationConfiguration configuration;

    public EventTreeCellRenderer(VisualizationConfiguration configuration) {
        super();

        this.configuration = configuration;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value, boolean selected, boolean expanded,
                                                  boolean isLeaf, int row, boolean focused) {
        // because of some weird bug the text color has to be set before calling super
        // and the icons have to be set after calling super
        String s = value.toString();
        Color color = configuration.getColor(EventType.fromString(s.toLowerCase()));
        setTextNonSelectionColor(color);
        setTextSelectionColor(color.getRGB() != Color.BLACK.getRGB() ? color : Color.WHITE);

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
