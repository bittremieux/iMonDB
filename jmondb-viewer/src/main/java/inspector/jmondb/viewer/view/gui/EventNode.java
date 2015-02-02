package inspector.jmondb.viewer.view.gui;

/*
 * #%L
 * jMonDB Viewer
 * %%
 * Copyright (C) 2014 InSPECtor
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

import javax.swing.tree.DefaultMutableTreeNode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
	public int compareTo(EventNode o) {
		return getEvent().getDate().compareTo(o.getEvent().getDate());
	}
}
