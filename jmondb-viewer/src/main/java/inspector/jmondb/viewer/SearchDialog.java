package inspector.jmondb.viewer;

import inspector.jmondb.model.Metadata;

import javax.swing.*;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class SearchDialog extends JPanel {

	public SearchDialog(List<Metadata> metadata) {
		setLayout(new SpringLayout());

		SortedSet<String> names = new TreeSet<>();
		SortedSet<String> values = new TreeSet<>();
		for(Metadata md : metadata) {
			names.add(md.getName());
			values.add(md.getValue());
		}

		// add new search setting
		add(new JLabel());
		JLabel labelAdd = new JLabel("Specify additional metadata search settings");
		add(labelAdd);
		JButton buttonAdd = new JButton(new ImageIcon(getClass().getResource("/images/add.png")));
		add(buttonAdd);
		add(new JLabel());

		// metadata search settings
		JComboBox<String> comboBoxMetadata = new JComboBox<>(names.toArray(new String[names.size()]));
		add(comboBoxMetadata);

		JComboBox<String> comboBoxOperator = new JComboBox<>(new String[] { "=", "!=" });
		add(comboBoxOperator);

		JComboBox<String> comboBoxValue = new JComboBox<>(values.toArray(new String[values.size()]));
		add(comboBoxValue);

		JButton buttonRemove = new JButton(new ImageIcon(getClass().getResource("/images/remove.png")));
		add(buttonRemove);

		SpringUtilities.makeCompactGrid(this, 2, 4, 6, 6, 6, 6);
	}
}
