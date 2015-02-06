package inspector.jmondb.viewer.viewmodel;

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

import inspector.jmondb.model.Event;
import inspector.jmondb.model.EventType;
import inspector.jmondb.viewer.view.gui.EventConfigurationPanel;
import inspector.jmondb.viewer.view.gui.EventMarkers;
import inspector.jmondb.viewer.view.gui.EventNode;
import inspector.jmondb.viewer.view.gui.EventTree;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.List;

public class EventsViewModel {

    private EventTree eventTree;
    private EventMarkers eventMarkers;

    private EventConfigurationPanel eventConfigurationPanel;

    public EventsViewModel(EventTree eventTree, EventMarkers eventMarkers, EventConfigurationPanel eventConfigurationPanel) {
        this.eventTree = eventTree;
        this.eventMarkers = eventMarkers;
        this.eventConfigurationPanel = eventConfigurationPanel;
    }

    public List<Event> getAll() {
        List<Event> events = new ArrayList<>(eventTree.getTree().getRowCount());
        TreeNode rootNode = (TreeNode) eventTree.getTree().getModel().getRoot();
        for(int i = 0; i < rootNode.getChildCount(); i++) {
            TreeNode childNode = rootNode.getChildAt(i);
            for(int j = 0; j < childNode.getChildCount(); j++) {
                EventNode eventNode = (EventNode) childNode.getChildAt(j);
                events.add(eventNode.getEvent());
            }
        }
        return events;
    }

    public void add(Event event) {
        eventTree.addNode(event);
        eventMarkers.addMarker(event);
    }

    public void remove(Event event) {
        eventTree.removeNode(event);
        eventMarkers.removeMarker(event);
    }

    public void clearAll() {
        eventTree.clear();
        eventMarkers.clear();
    }

    public List<EventType> getDisplayedEventTypes() {
        List<EventType> displayed = new ArrayList<>(EventType.values().length);
        for(EventType type : EventType.values()) {
            if(eventConfigurationPanel.checkBoxIsSelected(type)) {
                displayed.add(type);
            }
        }
        return displayed;
    }
}
