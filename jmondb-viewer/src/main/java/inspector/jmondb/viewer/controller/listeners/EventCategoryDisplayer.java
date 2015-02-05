package inspector.jmondb.viewer.controller.listeners;

/*
 * #%L
 * jMonDB Viewer
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

import inspector.jmondb.model.EventType;
import inspector.jmondb.viewer.controller.GraphController;
import inspector.jmondb.viewer.view.gui.EventConfigurationPanel;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class EventCategoryDisplayer implements ItemListener {

    private EventConfigurationPanel eventConfigurationPanel;

    private GraphController graphController;

    public EventCategoryDisplayer(ViewerFrame viewer, GraphController graphController) {
        this.eventConfigurationPanel = viewer.getEventPanel().getEventConfigurationPanel();

        this.graphController = graphController;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        ItemSelectable source = e.getItemSelectable();

        EventType category = eventConfigurationPanel.getCheckBoxEventType((JCheckBox) source);
        boolean display = e.getStateChange() == ItemEvent.SELECTED;

        graphController.displayEventCategory(category, display);
    }
}
