package inspector.jmondb.viewer;

import com.google.common.collect.ImmutableMap;
import inspector.jmondb.io.IMonDBWriter;
import inspector.jmondb.model.*;
import inspector.jmondb.model.Event;
import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBReader;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.persistence.EntityManagerFactory;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
	private DefaultMutableTreeNode nodeCalibration;
	private HashMap<Date, ValueMarker> markerCalibration;
	private DefaultMutableTreeNode nodeMaintenance;
	private HashMap<Date, ValueMarker> markerMaintenance;
	private DefaultMutableTreeNode nodeIncident;
	private HashMap<Date, ValueMarker> markerIncident;

	private JTree treeEvents;

	private JCheckBox checkBoxCalibration;
	private JCheckBox checkBoxMaintenace;
	private JCheckBox checkBoxIncident;

	// connection to the iMonDB
	private EntityManagerFactory emf;
	private IMonDBReader dbReader;
	private IMonDBWriter dbWriter;

	public static void main(String[] args) {

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
		frameParent.setBackground(Color.WHITE);

		UIManager.put("OptionPane.background", Color.white);
		UIManager.put("Panel.background", Color.white);

		JPanel panelParent = new JPanel(new BorderLayout());
		panelParent.setBackground(Color.WHITE);
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
		panelSelection.setBackground(Color.WHITE);
		panelParent.add(panelSelection, BorderLayout.PAGE_START);

		panelGraph = new JPanel(new BorderLayout());
		panelGraph.setBackground(Color.WHITE);
		panelParent.add(panelGraph, BorderLayout.CENTER);

		panelDbConnection.setBackground(Color.WHITE);
		panelParent.add(panelDbConnection, BorderLayout.PAGE_END);

		panelEvents.setBackground(Color.WHITE);
		panelParent.add(panelEvents, BorderLayout.LINE_END);
	}

	private void createSelectionPanel(JPanel panelSelection) {
		JButton buttonConnectToDatabase = new JButton("Connect to database");
		buttonConnectToDatabase.addActionListener(new ListenerConnectToDatabase());
		panelSelection.add(buttonConnectToDatabase);

		JLabel labelInstrument = new JLabel("Instrument");
		panelSelection.add(labelInstrument);
		comboBoxInstrument = new JComboBox<>();
		comboBoxInstrument.addActionListener(new ListenerLoadInstrumentEvents());
		comboBoxInstrument.setPreferredSize(new Dimension(250, 25));
		comboBoxInstrument.setMaximumSize(new Dimension(250, 25));
		panelSelection.add(comboBoxInstrument);

		JLabel labelProperty = new JLabel("Property");
		panelSelection.add(labelProperty);
		comboBoxProperty = new JComboBox<>();
		comboBoxProperty.setPreferredSize(new Dimension(450, 25));
		comboBoxProperty.setMaximumSize(new Dimension(450, 25));
		panelSelection.add(comboBoxProperty);

		JButton buttonAdvanced = new JButton(new ImageIcon(getClass().getResource("/images/search.png"), "advanced search settings"));
		buttonAdvanced.setToolTipText("advanced search settings");
		buttonAdvanced.addActionListener(new ListenerAdvancedSettings());
		panelSelection.add(buttonAdvanced);

		System.out.println(buttonAdvanced.getPreferredSize());
		System.out.println(comboBoxProperty.getPreferredSize());

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
		//TODO: possibly set alternative icons
		treeEvents.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
														  Object value, boolean selected, boolean expanded,
														  boolean isLeaf, int row, boolean focused) {
				String s = value.toString();
				switch(s) {
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
				return super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
			}
		});

		JScrollPane scrollPaneEvents = new JScrollPane(treeEvents);

		nodeCalibration = new DefaultMutableTreeNode("Calibration");
		nodeEvents.add(nodeCalibration);
		nodeMaintenance = new DefaultMutableTreeNode("Maintenance");
		nodeEvents.add(nodeMaintenance);
		nodeIncident = new DefaultMutableTreeNode("Incident");
		nodeEvents.add(nodeIncident);

		expandEventsTree();

		JPanel buttonsPanel = new JPanel(new GridLayout(0, 3));
		JButton buttonAdd = new JButton("Add");
		buttonAdd.addActionListener(new ListenerAddEvent());
		buttonsPanel.add(buttonAdd);
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
			markerCalibration.values().forEach(plot::removeDomainMarker);
			markerMaintenance.values().forEach(plot::removeDomainMarker);
			markerIncident.values().forEach(plot::removeDomainMarker);
		}
		// remove from events panel
		treeEvents.clearSelection();
		nodeIncident.removeAllChildren();
		nodeMaintenance.removeAllChildren();
		nodeCalibration.removeAllChildren();
		DefaultTreeModel treeModel = ((DefaultTreeModel) treeEvents.getModel());
		treeModel.reload();
		// remove markers
		markerIncident.clear();
		markerMaintenance.clear();
		markerCalibration.clear();
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
						frameParent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						try {
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

							// fill in possible properties in the combo box
							comboBoxProperty.removeAllItems();
							List<Object[]> values = dbReader.getFromCustomQuery("SELECT prop.name, prop.accession FROM Property prop WHERE prop.isNumeric IS TRUE ORDER BY prop.name", Object[].class);
							for(Object[] value : values)
								comboBoxProperty.addItem(new PropertyComboBoxItem((String)value[0], (String)value[1]));

							// show the connection information
							labelDbConnection.setText("Connected to " + connectionDialog.getUserName() + "@" + connectionDialog.getHost() + "/" + connectionDialog.getDatabase());
							labelDbIcon.setIcon(iconConnected);
						}
						catch(Exception e1) {
							closeDbConnection();
							JOptionPane.showMessageDialog(frameParent, "<html><b>Could not connect to the database</b></html>\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}

						frameParent.setCursor(Cursor.getDefaultCursor());
					}
				};
				dbConnector.start();
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

	private class ListenerAdvancedSettings implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if(dbReader != null) {
				// get all unique metadata names for the selected instrument
				List<Metadata> metadata = dbReader.getFromCustomQuery("SELECT md FROM Metadata md WHERE md.run.instrument.name = :instName", Metadata.class, ImmutableMap.of("instName", (String) comboBoxInstrument.getSelectedItem()));

				// create dialog
				SearchDialog dialog = new SearchDialog(metadata);

				int option = JOptionPane.showConfirmDialog(frameParent, dialog, "Advanced search settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

				if(option == JOptionPane.OK_OPTION) {


				}
			}
		}
	}

	private class ListenerShowGraph implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			if(dbReader != null) {
				Thread graphThread = new Thread() {
					public void run() {
						frameParent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						String instrumentName = (String) comboBoxInstrument.getSelectedItem();
						PropertyComboBoxItem property = (PropertyComboBoxItem) comboBoxProperty.getSelectedItem();

						// check whether the selected property is numeric
						Boolean isNumeric = dbReader.getFromCustomQuery("SELECT isNumeric FROM Property prop WHERE accession = :accession", Boolean.class, ImmutableMap.of("accession", property.getAccession())).get(0);
						if(!isNumeric)
							JOptionPane.showMessageDialog(frameParent, "Property <" + property.getName() + "> is not numeric.", "Warning", JOptionPane.WARNING_MESSAGE);

						// load all values for the property and instrument
						Map<String, String> parameters = ImmutableMap.of("instName", instrumentName, "propAccession", property.getAccession());
						List<Object[]> values = dbReader.getFromCustomQuery("SELECT val, val.originatingRun.sampleDate FROM Value val WHERE val.originatingRun.instrument.name = :instName AND val.definingProperty.accession = :propAccession ORDER BY val.originatingRun.sampleDate", Object[].class, parameters);

						if(values.size() == 0)
							JOptionPane.showMessageDialog(frameParent, "No matching values found.", "Warning", JOptionPane.WARNING_MESSAGE);
						else {
							// add data
							XYSeries medianSeries = new XYSeries("Median");
							XYSeries q1Series = new XYSeries("Q1");
							XYSeries q3Series = new XYSeries("Q3");
							XYSeries minSeries = new XYSeries("Min");
							XYSeries maxSeries = new XYSeries("Max");
							for(Object[] objects : values) {
								Value value = (Value) objects[0];
								Timestamp time = (Timestamp) objects[1];
								medianSeries.add(time.getTime(), value.getMedian());
								q1Series.add(time.getTime(), value.getQ1());
								q3Series.add(time.getTime(), value.getQ3());
								minSeries.add(time.getTime(), value.getMin());
								maxSeries.add(time.getTime(), value.getMax());
							}

							XYSeriesCollection medianCollection = new XYSeriesCollection(medianSeries);
							XYSeriesCollection q1q3Collection = new XYSeriesCollection();
							q1q3Collection.addSeries(q1Series);
							q1q3Collection.addSeries(q3Series);
							XYSeriesCollection minMaxCollection = new XYSeriesCollection();
							minMaxCollection.addSeries(minSeries);
							minMaxCollection.addSeries(maxSeries);

							// renderer
							XYItemRenderer medianRenderer = new XYLineAndShapeRenderer();
							medianRenderer.setSeriesPaint(0, Color.BLACK);
							medianRenderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
							XYDifferenceRenderer q1q3Renderer = new XYDifferenceRenderer(Color.GRAY, Color.GRAY, true);
							q1q3Renderer.setSeriesPaint(0, Color.GRAY);
							q1q3Renderer.setSeriesPaint(1, Color.GRAY);
							q1q3Renderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
							q1q3Renderer.setSeriesShape(1, new Ellipse2D.Double(-2, -2, 4, 4));
							XYDifferenceRenderer minMaxRenderer = new XYDifferenceRenderer(Color.LIGHT_GRAY, Color.LIGHT_GRAY, true);
							minMaxRenderer.setSeriesPaint(0, Color.LIGHT_GRAY);
							minMaxRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
							minMaxRenderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
							minMaxRenderer.setSeriesShape(1, new Ellipse2D.Double(-2, -2, 4, 4));

							// create axis
							DateAxis dateAxis = new DateAxis("Date");
							dateAxis.setDateFormatOverride(new SimpleDateFormat("dd/MM/yyyy"));
							dateAxis.setVerticalTickLabels(true);

							NumberAxis valueAxis = new NumberAxis("Value");
							valueAxis.setAutoRangeIncludesZero(false);

							// create plot and draw graph
							XYPlot plot = new XYPlot();
							plot.setDomainAxis(dateAxis);
							plot.setRangeAxis(valueAxis);
							plot.setDataset(0, medianCollection);
							plot.setDataset(1, q1q3Collection);
							plot.setDataset(2, minMaxCollection);
							plot.setRenderer(0, medianRenderer);
							plot.setRenderer(1, q1q3Renderer);
							plot.setRenderer(2, minMaxRenderer);
							JFreeChart chart = new JFreeChart(property.getName(), plot);
							chart.setBackgroundPaint(Color.WHITE);
							chartPanel = new ChartPanel(chart, false, true, false, true, false);
							chart.removeLegend();

							panelGraph.removeAll();
							panelGraph.add(chartPanel, BorderLayout.CENTER);
							panelGraph.validate();

							drawEvents();
						}

						frameParent.setCursor(Cursor.getDefaultCursor());
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
						Instrument instrument = dbReader.getInstrument(dialog.getInstrumentName(), true);
						Event event = new Event(instrument, dialog.getDate(), dialog.getType(), dialog.getDescription(), dialog.getPicture());

						// write the event to the database
						dbWriter.writeOrUpdateEvent(event);

						// add to the events list and create a marker
						ValueMarker marker = null;
						boolean toDraw = false;
						switch(event.getType()) {
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
				// update the changed information
				// only the description and the picture can be changed
				boolean toWrite = (event.getDescription() == null && dialog.getDescription() != null) ||
						(event.getDescription() != null && !event.getDescription().equals(dialog.getDescription())) ||
						!Arrays.equals(event.getPicture(), dialog.getPicture());
				event.setDescription(dialog.getDescription());
				event.setPicture(dialog.getPicture());

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
				if(source == checkBoxCalibration) {
					if(e.getStateChange() == ItemEvent.DESELECTED)
						markerCalibration.values().forEach(plot::removeDomainMarker);
					else
						markerCalibration.values().forEach(plot::addDomainMarker);
				}
				if(source == checkBoxMaintenace) {
					if(e.getStateChange() == ItemEvent.DESELECTED)
						markerMaintenance.values().forEach(plot::removeDomainMarker);
					else
						markerMaintenance.values().forEach(plot::addDomainMarker);
				}
				if(source == checkBoxIncident) {
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
			try {
				URI uri = new URI("https://bitbucket.org/proteinspector/jmondb");
				class OpenUrlAction implements ActionListener {
					public void actionPerformed(ActionEvent e) {
						if(Desktop.isDesktopSupported()) {
							try {
								Desktop.getDesktop().browse(uri);
							} catch(IOException e2) {
								JOptionPane.showMessageDialog(frameParent, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							}
						} else {
							JOptionPane.showMessageDialog(frameParent, "Could not open the website", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}

				JButton button = new JButton();
				button.setText("<html>For more information, please visit our <font color=\"#000099\"><u>website</u></font>.</html>");
				button.setHorizontalAlignment(SwingConstants.LEFT);
				button.setBorderPainted(false);
				button.setOpaque(false);
				button.setBackground(Color.WHITE);

				button.setToolTipText(uri.toString());
				button.addActionListener(new OpenUrlAction());

				JOptionPane.showMessageDialog(frameParent, button, "About", JOptionPane.INFORMATION_MESSAGE);

			} catch(URISyntaxException e1) {
				JOptionPane.showMessageDialog(frameParent, "Could not open the website", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
