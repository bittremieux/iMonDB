package inspector.jmondb.viewer;

import inspector.jmondb.intervention.Intervention;

import javax.swing.tree.DefaultMutableTreeNode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class InterventionNode extends DefaultMutableTreeNode {

	private Intervention intervention;

	public InterventionNode(Intervention i) {
		setIntervention(i);
	}

	public Intervention getIntervention() {
		return intervention;
	}

	public void setIntervention(Intervention i) {
		this.intervention = i;
	}

	@Override
	public String toString() {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		return df.format(intervention.getDate());
	}
}
