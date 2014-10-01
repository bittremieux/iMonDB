package inspector.jmondb.viewer;

import inspector.jmondb.model.Event;

import javax.swing.tree.DefaultMutableTreeNode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class InterventionNode extends DefaultMutableTreeNode implements Comparable<InterventionNode> {

	private Event intervention;

	public InterventionNode(Event i) {
		super();
		setIntervention(i);
	}

	public Event getIntervention() {
		return intervention;
	}

	public void setIntervention(Event i) {
		this.intervention = i;
	}

	@Override
	public String toString() {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		return df.format(intervention.getDate());
	}

	@Override
	public int compareTo(InterventionNode o) {
		//TODO
		return getIntervention().getDate().compareTo(o.getIntervention().getDate());
	}
}
