package inspector.jmondb.viewer;

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

import com.google.common.collect.ImmutableMap;
import inspector.jmondb.io.IMonDBWriter;
import inspector.jmondb.model.*;
import inspector.jmondb.model.Event;
import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBReader;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import javax.persistence.EntityManagerFactory;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Viewer extends JPanel {

	private JFrame frameParent;

	// combo boxes that will be populated when connected to the database
	private JComboBox<String> comboBoxInstrument;
	private JComboBox<PropertyComboBoxItem> comboBoxProperty;

	// graph showing the values over time
	private JPanel panelGraph;
	private ChartPanel chartPanel;

	// database connection information
	private JLabel labelDbConnection;
	private JLabel labelDbIcon;
	private static ImageIcon iconNotConnected = new ImageIcon(Viewer.class.getResource("/images/nok.png"), "not connected");
	private static ImageIcon iconConnected = new ImageIcon(Viewer.class.getResource("/images/ok.png"), "connected");

	// events information
	private DefaultMutableTreeNode nodeUndefined;
	private HashMap<Date, ValueMarker> markerUndefined;
	private DefaultMutableTreeNode nodeCalibration;
	private HashMap<Date, ValueMarker> markerCalibration;
	private DefaultMutableTreeNode nodeMaintenance;
	private HashMap<Date, ValueMarker> markerMaintenance;
	private DefaultMutableTreeNode nodeIncident;
	private HashMap<Date, ValueMarker> markerIncident;

	private JTree treeEvents;

	private JCheckBox checkBoxUndefined;
	private JCheckBox checkBoxCalibration;
	private JCheckBox checkBoxMaintenace;
	private JCheckBox checkBoxIncident;

	// advanced search settings
	private SearchDialog advancedSearchDialog;

	// connection to the iMonDB
	private EntityManagerFactory emf;
	private IMonDBReader dbReader;
	private IMonDBWriter dbWriter;

	public static void main(String[] args) {

		try {
			// Nimbus look and feel
			for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// if Nimbus is not available, fall back to cross-platform
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception ignored) {
			}
		}

		// start viewer
		SwingUtilities.invokeLater(() -> {
			Viewer viewer = new Viewer();
			viewer.display();
		});
	}

	public void display() {
		frameParent.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frameParent.setMinimumSize(new Dimension(1280, 800));
		frameParent.setPreferredSize(new Dimension(1280, 800));
		frameParent.pack();
		frameParent.setVisible(true);
	}

	public Viewer() {
		frameParent = new JFrame("iMonDB Viewer");

		JPanel panelParent = new JPanel(new BorderLayout());
		frameParent.setContentPane(panelParent);

		// create menu bar
		frameParent.setJMenuBar(createMenuBar());

		// arrange panels
		JPanel panelSelection = new JPanel();
		JPanel panelDbConnection = new JPanel();
		JPanel panelEvents = new JPanel();
		arrangePanels(panelParent, panelSelection, panelDbConnection, panelEvents);

		// value selection panel
		createSelectionPanel(panelSelection);

		// database connection panel
		createDbConnectionPanel(panelDbConnection);

		// events panel
		markerUndefined = new HashMap<>();
		markerCalibration = new HashMap<>();
		markerMaintenance = new HashMap<>();
		markerIncident = new HashMap<>();
		createEventsPanel(panelEvents);
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		// file menu
		JMenu menuFile = new JMenu("File");
		JMenuItem menuItemConnectToDb = new JMenuItem("Connect to the database");
		menuItemConnectToDb.addActionListener(new ListenerConnectToDatabase());
		menuFile.add(menuItemConnectToDb);
		JMenuItem menuItemCloseDb = new JMenuItem("Disconnect from the database");
		menuItemCloseDb.addActionListener(new ListenerDisconnectFromDatabase());
		menuFile.add(menuItemCloseDb);

		menuFile.addSeparator();

		JMenuItem menuItemSaveGraph = new JMenuItem("Save graph as...");
		menuItemSaveGraph.addActionListener(new ListenerSaveGraph());
		menuFile.add(menuItemSaveGraph);
		JMenuItem menuItemExportEvents = new JMenuItem("Export event log as...");
		menuItemExportEvents.addActionListener(new ListenerExportEvents());
		menuFile.add(menuItemExportEvents);

		menuFile.addSeparator();

		JMenuItem menuItemQuit = new JMenuItem("Quit");
		menuItemQuit.addActionListener(new ListenerQuit());
		menuFile.add(menuItemQuit);

		// add to menu bar
		menuBar.add(menuFile);

		// help menu
		JMenu menuHelp = new JMenu("Help");
		JMenuItem menuItemAbout = new JMenuItem("About");
		menuItemAbout.addActionListener(new ListenerAbout());
		menuHelp.add(menuItemAbout);

		// add to menu bar
		menuBar.add(menuHelp);

		return menuBar;
	}

	private void arrangePanels(JPanel panelParent, JPanel panelSelection, JPanel panelDbConnection, JPanel panelEvents) {
		panelParent.add(panelSelection, BorderLayout.PAGE_START);

		panelGraph = new JPanel(new BorderLayout());
		panelParent.add(panelGraph, BorderLayout.CENTER);

		panelParent.add(panelDbConnection, BorderLayout.PAGE_END);

		panelParent.add(panelEvents, BorderLayout.LINE_END);
	}

	private void createSelectionPanel(JPanel panelSelection) {
		JButton buttonConnectToDatabase = new JButton("Connect to database");
		buttonConnectToDatabase.addActionListener(new ListenerConnectToDatabase());
		panelSelection.add(buttonConnectToDatabase);

		JLabel labelInstrument = new JLabel("Instrument");
		panelSelection.add(labelInstrument);
		comboBoxInstrument = new JComboBox<>();
		comboBoxInstrument.addActionListener(new ListenerClearGraph());
		comboBoxInstrument.addActionListener(new ListenerLoadInstrumentEvents());
		comboBoxInstrument.addActionListener(new ListenerCreateAdvancedSearchDialog());
		comboBoxInstrument.setPreferredSize(new Dimension(250, 25));
		comboBoxInstrument.setMaximumSize(new Dimension(250, 25));
		panelSelection.add(comboBoxInstrument);

		JLabel labelProperty = new JLabel("Property");
		panelSelection.add(labelProperty);
		ComboBoxModel<PropertyComboBoxItem> sortedComboBoxModel = new SortedComboBoxModel<>();
		comboBoxProperty = new JComboBox<>(sortedComboBoxModel);
		comboBoxProperty.setPreferredSize(new Dimension(450, 25));
		comboBoxProperty.setMaximumSize(new Dimension(450, 25));
		panelSelection.add(comboBoxProperty);

		JButton buttonAdvanced = new JButton(new ImageIcon(getClass().getResource("/images/search.png"), "advanced search settings"));
		buttonAdvanced.setToolTipText("advanced search settings");
		buttonAdvanced.addActionListener(new ListenerAdvancedSettings());
		panelSelection.add(buttonAdvanced);

		JButton buttonShowGraph = new JButton("Show graph");
		buttonShowGraph.addActionListener(new ListenerShowGraph());
		panelSelection.add(buttonShowGraph);
	}

	private void createDbConnectionPanel(JPanel panelDbConnection) {
		panelDbConnection.add(new JLabel("Database connection:"));

		labelDbConnection = new JLabel("Not connected");
		panelDbConnection.add(labelDbConnection);

		labelDbIcon = new JLabel(iconNotConnected);
		panelDbConnection.add(labelDbIcon);
	}

	private void createEventsPanel(JPanel eventsPanel) {
		BorderLayout eventsLayout = new BorderLayout();
		eventsLayout.setVgap(25);
		eventsPanel.setLayout(eventsLayout);

		// create checkboxes
		JPanel panelCheckBoxes = new JPanel(new GridLayout(0, 1));
		panelCheckBoxes.add(new JLabel("Show events:"));
		checkBoxUndefined = new JCheckBox("Undefined");
		checkBoxUndefined.setSelected(true);
		panelCheckBoxes.add(checkBoxUndefined);
		checkBoxCalibration = new JCheckBox("Calibration");
		checkBoxCalibration.setSelected(true);
		panelCheckBoxes.add(checkBoxCalibration);
		checkBoxMaintenace = new JCheckBox("Maintenance");
		checkBoxMaintenace.setSelected(true);
		panelCheckBoxes.add(checkBoxMaintenace);
		checkBoxIncident = new JCheckBox("Incident");
		checkBoxIncident.setSelected(true);
		panelCheckBoxes.add(checkBoxIncident);

		ItemListener checkBoxListener = new ListenerCheckBox();
		checkBoxUndefined.addItemListener(checkBoxListener);
		checkBoxCalibration.addItemListener(checkBoxListener);
		checkBoxMaintenace.addItemListener(checkBoxListener);
		checkBoxIncident.addItemListener(checkBoxListener);

		eventsPanel.add(panelCheckBoxes, BorderLayout.PAGE_START);

		// create events tree view
		DefaultMutableTreeNode nodeEvents = new DefaultMutableTreeNode("Events");
		treeEvents = new JTree(nodeEvents);
		// mouse listener to create context menus on right click
		ActionListener removeListener = new ListenerRemoveEvent();
		treeEvents.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(treeEvents.getSelectionPath() != null &&
						treeEvents.getSelectionPath().getLastPathComponent() instanceof EventNode) {
					EventNode selectedNode = (EventNode) treeEvents.getSelectionPath().getLastPathComponent();
					if(SwingUtilities.isRightMouseButton(e)) {
						// highlight relevant item
						int row = treeEvents.getClosestRowForLocation(e.getX(), e.getY());
						treeEvents.setSelectionRow(row);

						// show pop-up menu
						JPopupMenu popupMenu = new JPopupMenu();
						JMenuItem itemEdit = new JMenuItem("Edit");
						itemEdit.addActionListener(new ListenerEditEvent(selectedNode.getEvent()));
						popupMenu.add(itemEdit);
						JMenuItem itemRemove = new JMenuItem("Remove");
						itemRemove.addActionListener(removeListener);
						popupMenu.add(itemRemove);

						popupMenu.show(e.getComponent(), e.getX(), e.getY());
					} else if(e.getClickCount() == 2) {
						new ListenerEditEvent(selectedNode.getEvent()).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
					}
				}
			}
		});
		ImageIcon eventIcon = new ImageIcon(Viewer.class.getResource("/images/event.png"));
		ImageIcon undefinedIcon = new ImageIcon(Viewer.class.getResource("/images/undefined.png"));
		ImageIcon calibrationIcon = new ImageIcon(Viewer.class.getResource("/images/calibration.png"));
		ImageIcon maintenanceIcon = new ImageIcon(Viewer.class.getResource("/images/maintenance.png"));
		ImageIcon incidentIcon = new ImageIcon(Viewer.class.getResource("/images/incident.png"));
		treeEvents.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
														  Object value, boolean selected, boolean expanded,
														  boolean isLeaf, int row, boolean focused) {
				// because of some weird bug the text color has to be set before calling super
				// and the icons have to be set after calling super
				String s = value.toString();
				switch(s) {
					case "Undefined":
						setTextNonSelectionColor(Color.ORANGE);
						setTextSelectionColor(Color.ORANGE);
						break;
					case "Calibration":
						setTextNonSelectionColor(Color.GREEN);
						setTextSelectionColor(Color.GREEN);
						break;
					case "Maintenance":
						setTextNonSelectionColor(Color.BLUE);
						setTextSelectionColor(Color.BLUE);
						break;
					case "Incident":
						setTextNonSelectionColor(Color.RED);
						setTextSelectionColor(Color.RED);
						break;
					default:
						setTextNonSelectionColor(Color.BLACK);
						setTextSelectionColor(Color.WHITE);
						break;
				}
				super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
				switch(s) {
					case "Events":
						setIcon(eventIcon);
						setClosedIcon(eventIcon);
						setOpenIcon(eventIcon);
						break;
					case "Undefined":
						setIcon(undefinedIcon);
						setClosedIcon(undefinedIcon);
						setOpenIcon(undefinedIcon);
						break;
					case "Calibration":
						setIcon(calibrationIcon);
						setClosedIcon(calibrationIcon);
						setOpenIcon(calibrationIcon);
						break;
					case "Maintenance":
						setIcon(maintenanceIcon);
						setClosedIcon(maintenanceIcon);
						setOpenIcon(maintenanceIcon);
						break;
					case "Incident":
						setIcon(incidentIcon);
						setClosedIcon(incidentIcon);
						setOpenIcon(incidentIcon);
						break;
					default:
						break;
				}

				return this;
			}
		});

		JScrollPane scrollPaneEvents = new JScrollPane(treeEvents);

		nodeUndefined = new DefaultMutableTreeNode("Undefined");
		nodeEvents.add(nodeUndefined);
		nodeCalibration = new DefaultMutableTreeNode("Calibration");
		nodeEvents.add(nodeCalibration);
		nodeMaintenance = new DefaultMutableTreeNode("Maintenance");
		nodeEvents.add(nodeMaintenance);
		nodeIncident = new DefaultMutableTreeNode("Incident");
		nodeEvents.add(nodeIncident);

		expandEventsTree();

		JPanel buttonsPanel = new JPanel(new GridLayout(2, 2));
		buttonsPanel.setPreferredSize(new Dimension(250, 50));
		JButton buttonAdd = new JButton("Add");
		buttonAdd.addActionListener(new ListenerAddEvent());
		buttonsPanel.add(buttonAdd);
		JButton buttonExport = new JButton("Export");
		buttonExport.addActionListener(new ListenerExportEvents());
		buttonsPanel.add(buttonExport);
		JButton buttonRemove = new JButton("Remove");
		buttonRemove.addActionListener(removeListener);
		buttonsPanel.add(buttonRemove);
		JButton buttonClear = new JButton("Clear");
		buttonClear.addActionListener(new ListenerClearEvents());
		buttonsPanel.add(buttonClear);

		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.add(scrollPaneEvents, BorderLayout.CENTER);
		treePanel.add(buttonsPanel, BorderLayout.PAGE_END);

		eventsPanel.add(treePanel, BorderLayout.CENTER);
	}

	private void closeDbConnection() {
		// close emf
		if(emf != null && emf.isOpen())
			emf.close();
		emf = null;
		dbReader = null;
		dbWriter = null;
		// remove combo box values
		comboBoxInstrument.removeAllItems();
		comboBoxProperty.removeAllItems();
		// remove events
		clearEventVisualization();
		// remove advanced search settings
		advancedSearchDialog = null;
		// show information
		labelDbConnection.setText("Not connected");
		labelDbIcon.setIcon(iconNotConnected);
	}

	private void expandEventsTree() {
		for(int i = 0; i < treeEvents.getRowCount(); i++)
			treeEvents.expandRow(i);
	}

	private void drawEvents() {
		if(chartPanel != null) {
			XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();

			if(checkBoxUndefined.isSelected())
				markerUndefined.values().forEach(plot::addDomainMarker);
			else
				markerUndefined.values().forEach(plot::removeDomainMarker);
			if(checkBoxCalibration.isSelected())
				markerCalibration.values().forEach(plot::addDomainMarker);
			else
				markerCalibration.values().forEach(plot::removeDomainMarker);
			if(checkBoxMaintenace.isSelected())
				markerMaintenance.values().forEach(plot::addDomainMarker);
			else
				markerMaintenance.values().forEach(plot::removeDomainMarker);
			if(checkBoxIncident.isSelected())
				markerIncident.values().forEach(plot::addDomainMarker);
			else
				markerIncident.values().forEach(plot::removeDomainMarker);
		}
	}

	private void clearEventVisualization() {
		// remove from graph
		if(chartPanel != null) {
			XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
			markerUndefined.values().forEach(plot::removeDomainMarker);
			markerCalibration.values().forEach(plot::removeDomainMarker);
			markerMaintenance.values().forEach(plot::removeDomainMarker);
			markerIncident.values().forEach(plot::removeDomainMarker);
		}
		// remove from events panel
		treeEvents.clearSelection();
		nodeIncident.removeAllChildren();
		nodeMaintenance.removeAllChildren();
		nodeCalibration.removeAllChildren();
		nodeUndefined.removeAllChildren();
		DefaultTreeModel treeModel = ((DefaultTreeModel) treeEvents.getModel());
		treeModel.reload();
		// remove markers
		markerIncident.clear();
		markerMaintenance.clear();
		markerCalibration.clear();
		markerUndefined.clear();
	}

	private void sortEvents(DefaultMutableTreeNode parent) {
		List<EventNode> children = new ArrayList<>(parent.getChildCount());
		for(int i = 0; i< parent.getChildCount(); i++)
			children.add((EventNode) parent.getChildAt(i));
		Collections.sort(children);
		parent.removeAllChildren();
		children.forEach(parent::add);
	}

	private ValueMarker removeMarker(Event event) {
		switch(event.getType()) {
			case UNDEFINED:
				return markerUndefined.remove(event.getDate());
			case CALIBRATION:
				return markerCalibration.remove(event.getDate());
			case MAINTENANCE:
				return markerMaintenance.remove(event.getDate());
			case INCIDENT:
				return markerIncident.remove(event.getDate());
			default:
				return null;
		}
	}

	private class ListenerConnectToDatabase implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			// create connection dialog
			DatabaseConnectionDialog connectionDialog = new DatabaseConnectionDialog();

			int option = JOptionPane.showConfirmDialog(frameParent, connectionDialog, "Connect to the database", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if(option == JOptionPane.OK_OPTION) {
				Thread dbConnector = new Thread() {
					public void run() {
						try {
							frameParent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

							// first close an existing connection
							closeDbConnection();

							// create the new connection
							String password = !connectionDialog.getPassword().equals("") ? connectionDialog.getPassword() : null;
							emf = IMonDBManagerFactory.createMySQLFactory(connectionDialog.getHost(), connectionDialog.getPort(),
									connectionDialog.getDatabase(), connectionDialog.getUserName(), password);
							dbReader = new IMonDBReader(emf);
							dbWriter = new IMonDBWriter(emf);

							// fill in possible instruments in the combo box
							comboBoxInstrument.removeAllItems();
							List<String> instrumentNames = dbReader.getFromCustomQuery("SELECT inst.name FROM Instrument inst ORDER BY inst.name", String.class);
							instrumentNames.forEach(comboBoxInstrument::addItem);

							// create advanced search settings
							createAdvancedSearchDialog();
							// fill in possible properties in the combo box
							setProperties();

							// show the connection information
							labelDbConnection.setText("Connected to " + connectionDialog.getUserName() + "@" + connectionDialog.getHost() + "/" + connectionDialog.getDatabase());
							labelDbIcon.setIcon(iconConnected);
						} catch(Exception e1) {
							closeDbConnection();
							JOptionPane.showMessageDialog(frameParent, "<html><b>Could not connect to the database</b></html>\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						} finally {
							frameParent.setCursor(Cursor.getDefaultCursor());
						}
					}
				};
				dbConnector.start();
			}
		}
	}

	private void createAdvancedSearchDialog() {
		if(dbReader != null && comboBoxInstrument.getSelectedIndex() != -1) {
			// get all unique metadata names for the selected instrument
			List<Metadata> metadata = dbReader.getFromCustomQuery("SELECT md FROM Metadata md WHERE md.run.instrument.name = :instName", Metadata.class, ImmutableMap.of("instName", (String) comboBoxInstrument.getSelectedItem()));

			// create dialog (this implicitly resets it as well)
			advancedSearchDialog = new SearchDialog(metadata);
			setProperties();
		}
	}

	private void setProperties() {
		comboBoxProperty.removeAllItems();

		// load the instrument and all it's properties
		Instrument instrument = dbReader.getInstrument((String) comboBoxInstrument.getSelectedItem(), false, true);

		for(Iterator<Property> it = instrument.getPropertyIterator(); it.hasNext(); ) {
			Property property = it.next();

			if(property.getNumeric()) {
				if(advancedSearchDialog != null && advancedSearchDialog.getFilterString() != null) {
					if(StringUtils.containsIgnoreCase(property.getName(), advancedSearchDialog.getFilterString()))
						comboBoxProperty.addItem(new PropertyComboBoxItem(property.getName(), property.getAccession()));
				} else
					comboBoxProperty.addItem(new PropertyComboBoxItem(property.getName(), property.getAccession()));
			}
		}
	}

	private class ListenerDisconnectFromDatabase implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			closeDbConnection();
		}
	}

	private class ListenerLoadInstrumentEvents implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Thread eventLoader = new Thread() {
				public void run() {
					if(dbReader != null) {
						// remove old events
						clearEventVisualization();

						// add new events
						String instrumentName = (String) comboBoxInstrument.getSelectedItem();
						Map<String, String> parameters = ImmutableMap.of("name", instrumentName);
						List<Event> events = dbReader.getFromCustomQuery("SELECT event FROM Event event WHERE event.instrument.name = :name ORDER BY event.date", Event.class, parameters);

						for(Event event : events) {
							EventNode node = new EventNode(event);

							// add to the correct event type
							switch(event.getType()) {
								case UNDEFINED:
									nodeUndefined.add(node);
									markerUndefined.put(event.getDate(), new ValueMarker(event.getDate().getTime(), Color.ORANGE, new BasicStroke(1)));
									break;
								case CALIBRATION:
									nodeCalibration.add(node);
									markerCalibration.put(event.getDate(), new ValueMarker(event.getDate().getTime(), Color.GREEN, new BasicStroke(1)));
									break;
								case MAINTENANCE:
									nodeMaintenance.add(node);
									markerMaintenance.put(event.getDate(), new ValueMarker(event.getDate().getTime(), Color.BLUE, new BasicStroke(1)));
									break;
								case INCIDENT:
									nodeIncident.add(node);
									markerIncident.put(event.getDate(), new ValueMarker(event.getDate().getTime(), Color.RED, new BasicStroke(1)));
									break;
								default:
									break;
							}
						}
					}
					// show all events in the events panel (TODO: might be reconsidered later on)
					expandEventsTree();
					// show the events on the graph
					drawEvents();
				}
			};
			eventLoader.start();
		}
	}

	private class ListenerCreateAdvancedSearchDialog implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			createAdvancedSearchDialog();
		}
	}

	private class ListenerAdvancedSettings implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if(dbReader != null && advancedSearchDialog != null) {
				String oldFilter = advancedSearchDialog.getFilterString();

				String[] options = new String[] { "OK", "Cancel", "Reset" };
				int option = JOptionPane.showOptionDialog(frameParent, advancedSearchDialog, "Advanced search settings", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				if(option == JOptionPane.YES_OPTION) {
					// apply advanced settings
					// property filtering
					if((oldFilter == null && advancedSearchDialog.getFilterString() != null) ||
							(oldFilter != null && !oldFilter.equals(advancedSearchDialog.getFilterString()))) {
						setProperties();
					}
					// metadata only has to be applied when querying to show the graph
				}
				else if(option == JOptionPane.CANCEL_OPTION) {
					// remove advanced settings
					advancedSearchDialog.reset();
					if(oldFilter != null)
						setProperties();
				}
			}
		}
	}

	private class ListenerClearGraph implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			panelGraph.removeAll();
			panelGraph.revalidate();
			panelGraph.repaint();
		}
	}

	private class ListenerShowGraph implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			if(dbReader != null) {
				Thread graphThread = new Thread() {
					public void run() {
						try {
							frameParent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

							String instrumentName = (String) comboBoxInstrument.getSelectedItem();
							PropertyComboBoxItem property = (PropertyComboBoxItem) comboBoxProperty.getSelectedItem();
							if(property == null)
								JOptionPane.showMessageDialog(frameParent, "Invalid property selected.", "Warning", JOptionPane.WARNING_MESSAGE);
							else {
								// check whether the selected property is numeric
								Boolean isNumeric = dbReader.getFromCustomQuery("SELECT isNumeric FROM Property prop WHERE accession = :accession", Boolean.class, ImmutableMap.of("accession", property.getAccession())).get(0);
								if(!isNumeric)
									JOptionPane.showMessageDialog(frameParent, "Property <" + property.getName() + "> is not numeric.", "Warning", JOptionPane.WARNING_MESSAGE);
								else {
									// load all values for the property and instrument
									StringBuilder querySelectFrom = new StringBuilder("SELECT val, val.originatingRun.sampleDate FROM Value val");
									StringBuilder queryWhere = new StringBuilder("WHERE val.originatingRun.instrument.name = :instName AND val.definingProperty.accession = :propAccession");
									Map<String, String> parameters = new HashMap<>();
									parameters.put("instName", instrumentName);
									parameters.put("propAccession", property.getAccession());

									// set advanced search settings
									for(int i = 0; i < advancedSearchDialog.getMetadataCount(); i++) {
										querySelectFrom.append(" JOIN val.originatingRun.metadata md").append(i);

										if(i == 0)
											queryWhere.append(" AND (");
										else if(i > 0)
											queryWhere.append(" ").append(advancedSearchDialog.getMetadataCombination(i));
										queryWhere.append(" md").append(i).append(".name = :md").append(i).append("Name AND md")
												.append(i).append(".value ").append(advancedSearchDialog.getMetadataOperator(i))
												.append(" :md").append(i).append("Value");
										if(i == advancedSearchDialog.getMetadataCount() - 1)
											queryWhere.append(")");

										parameters.put("md" + i + "Name", advancedSearchDialog.getMetadataName(i));
										parameters.put("md" + i + "Value", advancedSearchDialog.getMetadataValue(i));
									}
									String query = querySelectFrom.toString() + " " + queryWhere.toString() + " ORDER BY val.originatingRun.sampleDate";

									List<Object[]> values = dbReader.getFromCustomQuery(query, Object[].class, parameters);

									if(values.size() == 0)
										JOptionPane.showMessageDialog(frameParent, "No matching values found.", "Warning", JOptionPane.WARNING_MESSAGE);
									else {
										// draw graph
										ValuePlot plot = new ValuePlot(values);
										JFreeChart chart = new JFreeChart(property.getName(), plot);
										chart.removeLegend();
										chartPanel = new ChartPanel(chart, false, true, false, true, false);

										panelGraph.removeAll();
										panelGraph.add(chartPanel, BorderLayout.CENTER);
										panelGraph.validate();

										drawEvents();
									}
								}
							}
						} finally {
							frameParent.setCursor(Cursor.getDefaultCursor());
						}
					}
				};
				graphThread.start();
			}
		}
	}

	private class ListenerSaveGraph implements  ActionListener {

		public void actionPerformed(ActionEvent e) {
			if(chartPanel != null)
				try {
					chartPanel.doSaveAs();
				} catch(IOException e1) {
					JOptionPane.showMessageDialog(frameParent, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			else
				JOptionPane.showMessageDialog(frameParent, "No graph available yet.", "Warning", JOptionPane.WARNING_MESSAGE);
		}
	}

	private class ListenerExportEvents implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if(dbWriter == null) {
				JOptionPane.showMessageDialog(frameParent, "Please connect to a database exporting an event log.", "Warning", JOptionPane.WARNING_MESSAGE);
			}
			else {
				// get all events in chronological order
				String instrumentName = (String) comboBoxInstrument.getSelectedItem();
				Map<String, String> parameters = ImmutableMap.of("name", instrumentName);
				List<Event> events = dbReader.getFromCustomQuery("SELECT event FROM Event event WHERE event.instrument.name = :name ORDER BY event.date", Event.class, parameters);

				if(events.size() == 0) {
					JOptionPane.showMessageDialog(frameParent, "No events found for instrument <" + instrumentName + "> to export.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
				else {
					// export to a file
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(new FileNameExtensionFilter("PDF documents", "pdf"));
					int returnVal = fileChooser.showSaveDialog(frameParent);
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						Thread eventExporter = new Thread() {
							public void run() {
								try {
									File file = fileChooser.getSelectedFile();
									if(FilenameUtils.getExtension(file.getName()).equals(""))
										file = new File(file.getAbsolutePath() + ".pdf");
									EventsReportWriter.writeReport(instrumentName, events, file);

								} catch(DRException | IOException e1) {
									JOptionPane.showMessageDialog(frameParent, e1.getMessage(), "Error while exporting the event log", JOptionPane.ERROR_MESSAGE);
								}
							}
						};
						eventExporter.start();
					}
				}
			}
		}
	}

	private class ListenerAddEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if(dbWriter == null) {
				JOptionPane.showMessageDialog(frameParent, "Please connect to a database before creating a new event.", "Warning", JOptionPane.WARNING_MESSAGE);
			}
			else {
				// create event dialog
				EventDialog dialog = new EventDialog(comboBoxInstrument);

				int option = JOptionPane.showConfirmDialog(frameParent, dialog, "Add an event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

				if(option == JOptionPane.OK_OPTION) {
					try {
						// create new event
						Instrument instrument = dbReader.getInstrument(dialog.getInstrumentName(), true, false);
						Event event = new Event(instrument, dialog.getDate(), dialog.getType(), dialog.getProblem(), dialog.getSolution(), dialog.getExtra());
						if(dialog.getAttachmentName() != null && dialog.getAttachmentContent() != null) {
							event.setAttachmentName(dialog.getAttachmentName());
							event.setAttachmentContent(dialog.getAttachmentContent());
						}

						// write the event to the database
						dbWriter.writeOrUpdateEvent(event);

						// add to the events list and create a marker
						ValueMarker marker = null;
						boolean toDraw = false;
						switch(event.getType()) {
							case UNDEFINED:
								nodeUndefined.add(new EventNode(event));
								sortEvents(nodeUndefined);

								marker = new ValueMarker(event.getDate().getTime(), Color.ORANGE, new BasicStroke(1));
								markerUndefined.put(event.getDate(), marker);
								toDraw = checkBoxUndefined.isSelected();
								break;
							case CALIBRATION:
								nodeCalibration.add(new EventNode(event));
								sortEvents(nodeCalibration);

								marker = new ValueMarker(event.getDate().getTime(), Color.GREEN, new BasicStroke(1));
								markerCalibration.put(event.getDate(), marker);
								toDraw = checkBoxCalibration.isSelected();
								break;
							case MAINTENANCE:
								nodeMaintenance.add(new EventNode(event));
								sortEvents(nodeMaintenance);

								marker = new ValueMarker(event.getDate().getTime(), Color.BLUE, new BasicStroke(1));
								markerMaintenance.put(event.getDate(), marker);
								toDraw = checkBoxMaintenace.isSelected();
								break;
							case INCIDENT:
								nodeIncident.add(new EventNode(event));
								sortEvents(nodeIncident);

								marker = new ValueMarker(event.getDate().getTime(), Color.RED, new BasicStroke(1));
								markerIncident.put(event.getDate(), marker);
								toDraw = checkBoxIncident.isSelected();
								break;
							default:
								break;
						}
						DefaultTreeModel treeModel = ((DefaultTreeModel) treeEvents.getModel());
						treeModel.reload();

						// show all events in the events panel
						expandEventsTree();
						// draw the event on the graph
						if(toDraw && chartPanel != null) {
							XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
							plot.addDomainMarker(marker);
						}
					} catch(NullPointerException npe) {
						JOptionPane.showMessageDialog(frameParent, npe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}

	private class ListenerRemoveEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if(treeEvents.getSelectionPath() != null &&
					treeEvents.getSelectionPath().getLastPathComponent() instanceof EventNode) {

				int option = JOptionPane.showConfirmDialog(frameParent, "Attention: The event will be removed from the database as well.", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

				if(option == JOptionPane.OK_OPTION) {
					EventNode selectedNode = (EventNode) treeEvents.getSelectionPath().getLastPathComponent();
					try {
						// remove from the database
						dbWriter.removeEvent((String) comboBoxInstrument.getSelectedItem(), selectedNode.getEvent().getDate());

						// remove from tree
						DefaultTreeModel treeModel = ((DefaultTreeModel) treeEvents.getModel());
						treeModel.removeNodeFromParent(selectedNode);
						// remove from graph
						ValueMarker marker = removeMarker(selectedNode.getEvent());
						if(chartPanel != null && marker != null) {
							XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
							plot.removeDomainMarker(marker);
						}
					} catch(Exception ex) {
						JOptionPane.showMessageDialog(frameParent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}

	private class ListenerEditEvent implements ActionListener {

		private Event event;

		public ListenerEditEvent(Event event) {
			this.event = event;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// create event dialog
			EventDialog dialog = new EventDialog(comboBoxInstrument, event);

			int option = JOptionPane.showConfirmDialog(frameParent, dialog, "Edit event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if(option == JOptionPane.OK_OPTION) {
				// update the changed event information
				boolean toWrite = false;

				if(event.getProblem() != null ? !event.getProblem().equals(dialog.getProblem()) : dialog.getProblem() != null) {
					event.setProblem(dialog.getProblem());
					toWrite = true;
				}
				if(event.getSolution() != null ? !event.getSolution().equals(dialog.getSolution()) : dialog.getSolution() != null) {
					event.setSolution(dialog.getSolution());
					toWrite = true;
				}
				if(event.getExtra() != null ? !event.getExtra().equals(dialog.getExtra()) : dialog.getExtra() != null) {
					event.setExtra(dialog.getExtra());
					toWrite = true;
				}
				if(event.getAttachmentName() != null && event.getAttachmentContent() != null ?
						!event.getAttachmentName().equals(dialog.getAttachmentName()) ||
								!Arrays.equals(event.getAttachmentContent(), dialog.getAttachmentContent()) :
						dialog.getAttachmentName() != null || dialog.getAttachmentContent() != null) {
					event.setAttachmentName(dialog.getAttachmentName());
					event.setAttachmentContent(dialog.getAttachmentContent());
					toWrite = true;
				}

				if(toWrite)
					dbWriter.writeOrUpdateEvent(event);
			}
		}
	}

	private class ListenerClearEvents implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if(dbWriter != null) {
				int option = JOptionPane.showConfirmDialog(frameParent, "Attention: This will remove all events from the database!", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

				if(option == JOptionPane.OK_OPTION) {
					// remove the events from the database
					for(int i = 0; i < nodeUndefined.getChildCount(); i++) {
						Event event = ((EventNode) nodeUndefined.getChildAt(i)).getEvent();
						dbWriter.removeEvent(event.getInstrument().getName(), event.getDate());
					}
					for(int i = 0; i < nodeCalibration.getChildCount(); i++) {
						Event event = ((EventNode) nodeCalibration.getChildAt(i)).getEvent();
						dbWriter.removeEvent(event.getInstrument().getName(), event.getDate());
					}
					for(int i = 0; i < nodeMaintenance.getChildCount(); i++) {
						Event event = ((EventNode) nodeMaintenance.getChildAt(i)).getEvent();
						dbWriter.removeEvent(event.getInstrument().getName(), event.getDate());
					}
					for(int i = 0; i < nodeIncident.getChildCount(); i++) {
						Event event = ((EventNode) nodeIncident.getChildAt(i)).getEvent();
						dbWriter.removeEvent(event.getInstrument().getName(), event.getDate());
					}

					// remove the events from the viewer
					clearEventVisualization();
				}
			}
		}
	}

	private class ListenerCheckBox implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(chartPanel != null) {
				XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();

				Object source = e.getItemSelectable();
				// show or hide the specific events
				if(source == checkBoxUndefined) {
					if(e.getStateChange() == ItemEvent.DESELECTED)
						markerUndefined.values().forEach(plot::removeDomainMarker);
					else
						markerUndefined.values().forEach(plot::addDomainMarker);
				}
				else if(source == checkBoxCalibration) {
					if(e.getStateChange() == ItemEvent.DESELECTED)
						markerCalibration.values().forEach(plot::removeDomainMarker);
					else
						markerCalibration.values().forEach(plot::addDomainMarker);
				}
				else if(source == checkBoxMaintenace) {
					if(e.getStateChange() == ItemEvent.DESELECTED)
						markerMaintenance.values().forEach(plot::removeDomainMarker);
					else
						markerMaintenance.values().forEach(plot::addDomainMarker);
				}
				else if(source == checkBoxIncident) {
					if(e.getStateChange() == ItemEvent.DESELECTED)
						markerIncident.values().forEach(plot::removeDomainMarker);
					else
						markerIncident.values().forEach(plot::addDomainMarker);
				}
			}
		}
	}

	private class ListenerQuit implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			closeDbConnection();
			System.exit(0);
		}
	}

	private class ListenerAbout implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JLabelLink linkAbout = new JLabelLink("For more information, please visit our", "website", "https://bitbucket.org/proteinspector/jmondb", ".");
			JOptionPane.showMessageDialog(frameParent, linkAbout, "About", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
