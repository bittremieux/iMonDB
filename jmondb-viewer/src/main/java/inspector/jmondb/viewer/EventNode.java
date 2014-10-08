package inspector.jmondb.viewer;

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
		//TODO
		return getEvent().getDate().compareTo(o.getEvent().getDate());
	}
}
