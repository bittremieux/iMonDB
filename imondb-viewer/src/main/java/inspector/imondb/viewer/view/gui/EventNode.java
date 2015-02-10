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

import inspector.imondb.model.Event;

import javax.swing.tree.DefaultMutableTreeNode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class EventNode extends DefaultMutableTreeNode implements Comparable<EventNode> {

    private Event event;

    public EventNode(Event i) {
        super();
        setEvent(i);
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event i) {
        this.event = i;
    }

    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(event.getDate());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        final EventNode eventNode = (EventNode) o;
        return Objects.equals(event, eventNode.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event);
    }

    @Override
    public int compareTo(EventNode o) {
        return event.getDate().compareTo(o.event.getDate());
    }
}
