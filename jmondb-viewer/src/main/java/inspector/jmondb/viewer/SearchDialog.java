package inspector.jmondb.viewer;

import inspector.jmondb.model.Metadata;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class SearchDialog extends JPanel {

	private SortedSet<String> names;
	private SortedSet<String> values;

	private JTextField filterTextField;

	private JPanel settingsPanel;
	private JScrollPane settingsScrollPane;

	public SearchDialog(List<Metadata> metadata) {
		names = new TreeSet<>();
		values = new TreeSet<>();
		for(Metadata md : metadata) {
			names.add(md.getName());
			values.add(md.getValue());
		}

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(650, 400));

		// filter properties
		JPanel filterPropertiesPanel = new JPanel();
		JLabel filterLabel = new JLabel("Filter properties on name:");
		filterPropertiesPanel.add(filterLabel);
		filterTextField = new JTextField();
		filterTextField.setPreferredSize(new Dimension(250, 25));
		filterPropertiesPanel.add(filterTextField);

		// add metadata
		JPanel centerPanel = new JPanel(new BorderLayout());
		JPanel addMetadataPanel = new JPanel();
		JLabel labelAdd = new JLabel("Specify additional metadata search settings");
		addMetadataPanel.add(labelAdd);
		JButton buttonAdd = new JButton(new ImageIcon(getClass().getResource("/images/add.png")));
		if(metadata.size() == 0)
			buttonAdd.setEnabled(false);
		buttonAdd.addActionListener(new AddSettingsPanelListener());
		addMetadataPanel.add(buttonAdd);
		centerPanel.add(addMetadataPanel, BorderLayout.PAGE_START);

		settingsPanel = new JPanel(new GridLayout(0, 1));
		settingsScrollPane = new JScrollPane(settingsPanel);
		centerPanel.add(settingsScrollPane, BorderLayout.CENTER);

		add(filterPropertiesPanel, BorderLayout.PAGE_START);
		add(centerPanel, BorderLayout.CENTER);
	}

	private JPanel createCombinationPanel() {
		JPanel panel = new JPanel();
		panel.setName("combinationPanel");
		JComboBox<String> comboBox = new JComboBox<>(new String[] { "AND", "OR" });
		panel.add(comboBox);

		panel.setAlignmentX(Component.CENTER_ALIGNMENT);

		return panel;
	}

	private JPanel createSettingsPanel() {
		JPanel settingsPanel = new JPanel();
		JComboBox<String> comboBoxMetadata = new JComboBox<>(names.toArray(new String[names.size()]));
		comboBoxMetadata.setPreferredSize(new Dimension(250, 25));
		settingsPanel.add(comboBoxMetadata);

		JComboBox<String> comboBoxOperator = new JComboBox<>(new String[] { "=", "!=" });
		settingsPanel.add(comboBoxOperator);

		JComboBox<String> comboBoxValue = new JComboBox<>(values.toArray(new String[values.size()]));
		comboBoxValue.setPreferredSize(new Dimension(250, 25));
		settingsPanel.add(comboBoxValue);

		JButton buttonRemove = new JButton(new ImageIcon(getClass().getResource("/images/remove.png")));
		buttonRemove.addActionListener(new RemoveSettingsPanelListener());
		settingsPanel.add(buttonRemove);

		settingsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		return settingsPanel;
	}

	public String getFilterString() {
		if(filterTextField.getText().isEmpty())
			return null;
		else
			return filterTextField.getText();
	}

	public int getMetadataCount() {
		return (settingsPanel.getComponentCount() + 1) / 2;
	}

	@SuppressWarnings("unchecked")
	public String getMetadataName(int index) {
		return (String) ((JComboBox<String>) ((JPanel) settingsPanel.getComponent(index * 2)).getComponent(0)).getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	public String getMetadataOperator(int index) {
		return (String) ((JComboBox<String>) ((JPanel) settingsPanel.getComponent(index * 2)).getComponent(1)).getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	public String getMetadataValue(int index) {
		return (String) ((JComboBox<String>) ((JPanel) settingsPanel.getComponent(index * 2)).getComponent(2)).getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	public String getMetadataCombination(int index) {
		return (String) ((JComboBox<String>) ((JPanel) settingsPanel.getComponent(index * 2 - 1)).getComponent(0)).getSelectedItem();
	}

	public void reset() {
		filterTextField.setText(null);
		settingsPanel.removeAll();
	}

	private class AddSettingsPanelListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(settingsPanel.getComponentCount() > 0)
				settingsPanel.add(createCombinationPanel());
			settingsPanel.add(createSettingsPanel());
		}
	}

	private class RemoveSettingsPanelListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// remove the settings panel
			JButton button = (JButton) e.getSource();
			settingsPanel.remove(button.getParent());
			// check if a combination panel needs to be removed as well
			if(settingsPanel.getComponentCount() > 0) {
				// first
				if("combinationPanel".equals(settingsPanel.getComponent(0).getName()))
					settingsPanel.remove(0);
				// last
				if("combinationPanel".equals(settingsPanel.getComponent(settingsPanel.getComponentCount() - 1).getName()))
					settingsPanel.remove(settingsPanel.getComponentCount() - 1);
				// everything in between
				boolean prevIsCombination = false;
				for(int i = 0; i < settingsPanel.getComponentCount(); i++) {
					if("combinationPanel".equals(settingsPanel.getComponent(i).getName())) {
						if(prevIsCombination) {
							settingsPanel.remove(i);
							break;
						} else
							prevIsCombination = true;
					} else
						prevIsCombination = false;
				}
			}

			settingsPanel.validate();
			settingsPanel.repaint();
			settingsScrollPane.validate();
		}
	}
}
