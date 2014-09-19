package inspector.jmondb.viewer;

import inspector.jmondb.intervention.Intervention;
import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBReader;
import inspector.jmondb.model.Value;
import org.apache.commons.io.FilenameUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.persistence.EntityManagerFactory;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Viewer extends JPanel {

	private JFrame frameParent;

	// combo boxes that will be populated when connected to the database
	private JComboBox<String> comboBoxProject;
	private JComboBox<String> comboBoxValue;

	// graph showing the values over time
	private JPanel panelGraph;
	private ChartPanel chartPanel;

	// database connection information
	private JLabel labelDbConnection;
	private JLabel labelDbIcon;
	private static ImageIcon iconNotConnected = new ImageIcon(Viewer.class.getResource("/images/nok.png"), "not connected");
	private static ImageIcon iconConnected = new ImageIcon(Viewer.class.getResource("/images/ok.png"), "connected");

	// interventions information
	private DefaultMutableTreeNode nodeCalibration;
	private HashMap<Date, ValueMarker> markerCalibration;
	private DefaultMutableTreeNode nodeEvent;
	private HashMap<Date, ValueMarker> markerEvent;
	private DefaultMutableTreeNode nodeIncident;
	private HashMap<Date, ValueMarker> markerIncident;

	private JTree treeInterventions;

	private JCheckBox checkBoxCalibration;
	private JCheckBox checkBoxEvent;
	private JCheckBox checkBoxIncident;

	// connection to the iMonDB
	private EntityManagerFactory emf;
	private IMonDBReader dbReader;

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
		JPanel panelInterventions = new JPanel();
		arrangePanels(panelParent, panelSelection, panelDbConnection, panelInterventions);

		// value selection panel
		createSelectionPanel(panelSelection);

		// database connection panel
		createDbConnectionPanel(panelDbConnection);

		// interventions panel
		markerCalibration = new HashMap<>();
		markerEvent = new HashMap<>();
		markerIncident = new HashMap<>();
		createInterventionsPanel(panelInterventions);
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
		JMenuItem menuItemLoadInterventions = new JMenuItem("Load interventions");
		menuItemLoadInterventions.addActionListener(new ListenerLoadInterventions());
		menuFile.add(menuItemLoadInterventions);
		JMenuItem menuItemSaveInterventions = new JMenuItem("Save interventions");
		menuItemSaveInterventions.addActionListener(new ListenerSaveInterventions());
		menuFile.add(menuItemSaveInterventions);

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

	private void arrangePanels(JPanel panelParent, JPanel panelSelection, JPanel panelDbConnection, JPanel panelInterventions) {
		panelSelection.setBackground(Color.WHITE);
		panelParent.add(panelSelection, BorderLayout.PAGE_START);

		panelGraph = new JPanel(new BorderLayout());
		panelGraph.setBackground(Color.WHITE);
		panelParent.add(panelGraph, BorderLayout.CENTER);

		panelDbConnection.setBackground(Color.WHITE);
		panelParent.add(panelDbConnection, BorderLayout.PAGE_END);

		panelInterventions.setBackground(Color.WHITE);
		panelParent.add(panelInterventions, BorderLayout.LINE_END);
	}

	private void createSelectionPanel(JPanel panelSelection) {
		JButton buttonConnectToDatabase = new JButton("Connect to database");
		buttonConnectToDatabase.addActionListener(new ListenerConnectToDatabase());
		panelSelection.add(buttonConnectToDatabase);

		JLabel labelProject = new JLabel("Project");
		panelSelection.add(labelProject);
		comboBoxProject = new JComboBox<>();
		comboBoxProject.addActionListener(new ListenerFillProjectValues());
		comboBoxProject.setPreferredSize(new Dimension(250, 25));
		comboBoxProject.setMaximumSize(new Dimension(250, 25));
		panelSelection.add(comboBoxProject);

		JLabel labelValue = new JLabel("Value");
		panelSelection.add(labelValue);
		comboBoxValue = new JComboBox<>();
		comboBoxValue.setPreferredSize(new Dimension(500, 25));
		comboBoxValue.setMaximumSize(new Dimension(500, 25));
		panelSelection.add(comboBoxValue);

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

	private void createInterventionsPanel(JPanel interventionsPanel) {
		BorderLayout interventionsLayout = new BorderLayout();
		interventionsLayout.setVgap(25);
		interventionsPanel.setLayout(interventionsLayout);

		// create checkboxes
		JPanel panelCheckBoxes = new JPanel(new GridLayout(0, 1));
		panelCheckBoxes.add(new JLabel("Show interventions:"));
		checkBoxCalibration = new JCheckBox("Calibration");
		checkBoxCalibration.setSelected(true);
		panelCheckBoxes.add(checkBoxCalibration);
		checkBoxEvent = new JCheckBox("Event");
		checkBoxEvent.setSelected(true);
		panelCheckBoxes.add(checkBoxEvent);
		checkBoxIncident = new JCheckBox("Incident");
		checkBoxIncident.setSelected(true);
		panelCheckBoxes.add(checkBoxIncident);

		ItemListener checkBoxListener = new ListenerCheckBox();
		checkBoxCalibration.addItemListener(checkBoxListener);
		checkBoxEvent.addItemListener(checkBoxListener);
		checkBoxIncident.addItemListener(checkBoxListener);

		interventionsPanel.add(panelCheckBoxes, BorderLayout.PAGE_START);

		// create interventions tree view
		DefaultMutableTreeNode nodeInterventions = new DefaultMutableTreeNode("Interventions");
		treeInterventions = new JTree(nodeInterventions);
		// mouse listener to create context menus on right click
		ActionListener removeListener = new ListenerRemoveIntervention();
		treeInterventions.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					// highlight relevant item
					int row = treeInterventions.getClosestRowForLocation(e.getX(), e.getY());
					treeInterventions.setSelectionRow(row);

					// show pop-up menu
					JPopupMenu popupMenu = new JPopupMenu();
					JMenuItem itemEdit = new JMenuItem("Edit");
					InterventionNode selectedNode = (InterventionNode) treeInterventions.getSelectionPath().getLastPathComponent();
					itemEdit.addActionListener(new ListenerEditIntervention(selectedNode.getIntervention()));
					popupMenu.add(itemEdit);
					JMenuItem itemRemove = new JMenuItem("Remove");
					itemRemove.addActionListener(removeListener);
					popupMenu.add(itemRemove);

					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		//TODO: possibly set alternative icons
		treeInterventions.setCellRenderer(new DefaultTreeCellRenderer() {
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
					case "Event":
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

		JScrollPane scrollPaneInterventions = new JScrollPane(treeInterventions);

		nodeCalibration = new DefaultMutableTreeNode("Calibration");
		nodeInterventions.add(nodeCalibration);
		nodeEvent = new DefaultMutableTreeNode("Event");
		nodeInterventions.add(nodeEvent);
		nodeIncident = new DefaultMutableTreeNode("Incident");
		nodeInterventions.add(nodeIncident);

		expandInterventionsTree();

		JPanel buttonsPanel = new JPanel(new GridLayout(0, 3));
		JButton buttonAdd = new JButton("Add");
		buttonAdd.addActionListener(new ListenerAddIntervention());
		buttonsPanel.add(buttonAdd);
		JButton buttonRemove = new JButton("Remove");
		buttonRemove.addActionListener(removeListener);
		buttonsPanel.add(buttonRemove);
		JButton buttonClear = new JButton("Clear");
		buttonClear.addActionListener(new ListenerClearInterventions());
		buttonsPanel.add(buttonClear);

		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.add(scrollPaneInterventions, BorderLayout.CENTER);
		treePanel.add(buttonsPanel, BorderLayout.PAGE_END);

		interventionsPanel.add(treePanel, BorderLayout.CENTER);
	}

	private void closeDbConnection() {
		// close emf
		if(emf != null && emf.isOpen())
			emf.close();
		emf = null;
		dbReader = null;
		// remove combo box values
		comboBoxProject.removeAllItems();
		comboBoxValue.removeAllItems();
		// show information
		labelDbConnection.setText("Not connected");
		labelDbIcon.setIcon(iconNotConnected);
	}

	private void expandInterventionsTree() {
		for(int i = 0; i < treeInterventions.getRowCount(); i++)
			treeInterventions.expandRow(i);
	}

	private void drawInterventions() {
		if(chartPanel != null) {
			XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();

			if(checkBoxCalibration.isSelected())
				markerCalibration.values().forEach(plot::addDomainMarker);
			else
				markerCalibration.values().forEach(plot::removeDomainMarker);
			if(checkBoxEvent.isSelected())
				markerEvent.values().forEach(plot::addDomainMarker);
			else
				markerEvent.values().forEach(plot::removeDomainMarker);
			if(checkBoxIncident.isSelected())
				markerIncident.values().forEach(plot::addDomainMarker);
			else
				markerIncident.values().forEach(plot::removeDomainMarker);
		}
	}

	private void clearInterventions() {
		// remove from graph
		if(chartPanel != null) {
			XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
			markerCalibration.values().forEach(plot::removeDomainMarker);
			markerEvent.values().forEach(plot::removeDomainMarker);
			markerIncident.values().forEach(plot::removeDomainMarker);
		}
		// remove from interventions panel
		nodeIncident.removeAllChildren();
		nodeEvent.removeAllChildren();
		nodeCalibration.removeAllChildren();
		DefaultTreeModel treeModel = ((DefaultTreeModel) treeInterventions.getModel());
		treeModel.reload();
		// remove markers
		markerIncident.clear();
		markerEvent.clear();
		markerCalibration.clear();
	}

	private void sortInterventions(DefaultMutableTreeNode parent) {
		List<InterventionNode> children = new ArrayList<>(parent.getChildCount());
		for(int i = 0; i< parent.getChildCount(); i++)
			children.add((InterventionNode) parent.getChildAt(i));
		Collections.sort(children);
		parent.removeAllChildren();
		children.forEach(parent::add);
	}

	private ValueMarker removeMarker(Intervention intervention) {
		if(intervention.isIncident())
			return markerIncident.remove(intervention.getDate());
		else if(intervention.isEvent())
			return markerEvent.remove(intervention.getDate());
		else if(intervention.isCalibration())
			return markerCalibration.remove(intervention.getDate());
		else
			return null;
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
									connectionDialog.getUserName(), password, connectionDialog.getDatabase());
							dbReader = new IMonDBReader(emf);

							// fill in possible projects in the combo box
							List<String> projectLabels = dbReader.getFromCustomQuery("SELECT project.label FROM Project project ORDER BY project.label", String.class);
							projectLabels.forEach(comboBoxProject::addItem);

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

	private class ListenerFillProjectValues implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			Thread projectFiller = new Thread() {
				public void run() {
					if(dbReader != null) {
						frameParent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						comboBoxValue.removeAllItems();

						String projectLabel = (String) comboBoxProject.getSelectedItem();
						List<String> values = dbReader.getFromCustomQuery("SELECT DISTINCT val.name FROM Value val WHERE val.fromRun.fromProject.label = \"" + projectLabel + "\" ORDER BY val.name", String.class);
						values.forEach(comboBoxValue::addItem);

						frameParent.setCursor(Cursor.getDefaultCursor());
					}
				}
			};
			projectFiller.start();
		}
	}

	private class ListenerShowGraph implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			if(dbReader != null) {
				Thread graphThread = new Thread() {
					public void run() {
						frameParent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						String projectLabel = (String)comboBoxProject.getSelectedItem();
						String valueName = (String)comboBoxValue.getSelectedItem();

						List<Value> values = dbReader.getFromCustomQuery("SELECT val FROM Value val WHERE val.fromRun.fromProject.label = \"" + projectLabel + "\" AND val.name = \"" + valueName + "\" ORDER BY val.fromRun.sampleDate", Value.class);

						if(values.size() == 0)
							JOptionPane.showMessageDialog(frameParent, "No matching values found.", "Warning", JOptionPane.WARNING_MESSAGE);
						if(values.size() > 0 && !values.get(0).getNumeric())
							JOptionPane.showMessageDialog(frameParent, "Value <" + valueName + "> is not numeric.", "Warning", JOptionPane.WARNING_MESSAGE);
						else {
							// add data
							XYSeries medianSeries = new XYSeries("Median");
							XYSeries q1Series = new XYSeries("Q1");
							XYSeries q3Series = new XYSeries("Q3");
							XYSeries minSeries = new XYSeries("Min");
							XYSeries maxSeries = new XYSeries("Max");
							for(Value value : values) {
								medianSeries.add(value.getFromRun().getSampleDate().getTime(), value.getMedian());
								q1Series.add(value.getFromRun().getSampleDate().getTime(), value.getQ1());
								q3Series.add(value.getFromRun().getSampleDate().getTime(), value.getQ3());
								minSeries.add(value.getFromRun().getSampleDate().getTime(), value.getMin());
								maxSeries.add(value.getFromRun().getSampleDate().getTime(), value.getMax());
							}
							XYSeriesCollection q1Collection = new XYSeriesCollection();
							q1Collection.addSeries(medianSeries);
							q1Collection.addSeries(q1Series);
							XYSeriesCollection q3Collection = new XYSeriesCollection();
							q3Collection.addSeries(medianSeries);
							q3Collection.addSeries(q3Series);
							XYSeriesCollection minCollection = new XYSeriesCollection();
							minCollection.addSeries(q1Series);
							minCollection.addSeries(minSeries);
							XYSeriesCollection maxCollection = new XYSeriesCollection();
							maxCollection.addSeries(q3Series);
							maxCollection.addSeries(maxSeries);

							// renderer
							XYDifferenceRenderer q1Renderer = new XYDifferenceRenderer(Color.GRAY, Color.GRAY, true);
							q1Renderer.setSeriesPaint(0, Color.BLACK);
							q1Renderer.setSeriesPaint(1, Color.GRAY);
							q1Renderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
							q1Renderer.setSeriesShape(1, new Ellipse2D.Double(-2, -2, 4, 4));
							XYDifferenceRenderer q3Renderer = new XYDifferenceRenderer(Color.GRAY, Color.GRAY, true);
							q3Renderer.setSeriesPaint(0, Color.BLACK);
							q3Renderer.setSeriesPaint(1, Color.GRAY);
							q3Renderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
							q3Renderer.setSeriesShape(1, new Ellipse2D.Double(-2, -2, 4, 4));
							XYDifferenceRenderer minRenderer = new XYDifferenceRenderer(Color.LIGHT_GRAY, Color.LIGHT_GRAY, true);
							minRenderer.setSeriesPaint(0, Color.GRAY);
							minRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
							minRenderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
							minRenderer.setSeriesShape(1, new Ellipse2D.Double(-2, -2, 4, 4));
							XYDifferenceRenderer maxRenderer = new XYDifferenceRenderer(Color.LIGHT_GRAY, Color.LIGHT_GRAY, true);
							maxRenderer.setSeriesPaint(0, Color.GRAY);
							maxRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
							maxRenderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
							maxRenderer.setSeriesShape(1, new Ellipse2D.Double(-2, -2, 4, 4));

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
							plot.setDataset(0, q1Collection);
							plot.setDataset(1, q3Collection);
							plot.setDataset(2, minCollection);
							plot.setDataset(3, maxCollection);
							plot.setRenderer(0, q1Renderer);
							plot.setRenderer(1, q3Renderer);
							plot.setRenderer(2, minRenderer);
							plot.setRenderer(3, maxRenderer);
							JFreeChart chart = new JFreeChart(valueName, plot);
							chart.setBackgroundPaint(Color.WHITE);
							chartPanel = new ChartPanel(chart, false, true, false, true, false);
							chart.removeLegend();

							panelGraph.removeAll();
							panelGraph.add(chartPanel, BorderLayout.CENTER);
							panelGraph.validate();

							drawInterventions();
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

	private class ListenerLoadInterventions implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return FilenameUtils.getExtension(f.getName()).equalsIgnoreCase("csv");
				}

				@Override
				public String getDescription() {
					return "Interventions CSV file";
				}
			});
			int returnVal = fileChooser.showOpenDialog(frameParent);
			if(returnVal == JFileChooser.APPROVE_OPTION) {

				Thread interventionsLoader = new Thread() {
					public void run() {
						frameParent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						// remove previous interventions
						clearInterventions();

						// read new interventions
						File file = fileChooser.getSelectedFile();
						try {
							BufferedReader fileReader = new BufferedReader(new FileReader(file));
							SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
							String line = fileReader.readLine();	// skip header line
							while((line = fileReader.readLine()) != null) {
								String[] lineSplit = line.split(",", -1);
								Date date = sdf.parse(lineSplit[0]);
								boolean isCalibrationCheck = lineSplit[1].equals("1");
								boolean isCalibration = lineSplit[2].equals("1");
								boolean isEvent = lineSplit[3].equals("1");
								boolean isIncident = lineSplit[4].equals("1");
								String comment = lineSplit[5];

								Intervention intervention = new Intervention(date, isCalibrationCheck, isCalibration, isEvent, isIncident, comment);
								InterventionNode node = new InterventionNode(intervention);

								// add to the correct intervention type
								if(intervention.isIncident()) {
									nodeIncident.add(node);
									ValueMarker marker = new ValueMarker(intervention.getDate().getTime(), Color.RED, new BasicStroke(1));
									markerIncident.put(intervention.getDate(), marker);
								}
								else if(intervention.isEvent()) {
									nodeEvent.add(node);
									ValueMarker marker = new ValueMarker(intervention.getDate().getTime(), Color.BLUE, new BasicStroke(1));
									markerEvent.put(intervention.getDate(), marker);
								}
								else if(intervention.isCalibration()) {
									nodeCalibration.add(node);
									ValueMarker marker = new ValueMarker(intervention.getDate().getTime(), Color.GREEN, new BasicStroke(1));
									markerCalibration.put(intervention.getDate(), marker);
								}
							}

							// sort interventions
							sortInterventions(nodeIncident);
							sortInterventions(nodeEvent);
							sortInterventions(nodeCalibration);

						} catch(ParseException | IOException e1) {
							JOptionPane.showMessageDialog(frameParent, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}

						// show all interventions in the interventions panel (TODO: might be reconsidered later on)
						expandInterventionsTree();
						// show the interventions on the graph
						drawInterventions();

						frameParent.setCursor(Cursor.getDefaultCursor());
					}
				};
				interventionsLoader.start();
			}
		}
	}

	private class ListenerSaveInterventions implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if(markerCalibration.size() + markerEvent.size() + markerIncident.size() < 1) {
				JOptionPane.showMessageDialog(frameParent, "No interventions available yet.\nPlease load an interventions file or manually create some interventions first.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			else {

				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {
						return FilenameUtils.getExtension(f.getName()).equalsIgnoreCase("csv");
					}

					@Override
					public String getDescription() {
						return "Interventions CSV file";
					}
				});
				int returnVal = fileChooser.showSaveDialog(frameParent);
				if(returnVal == JFileChooser.APPROVE_OPTION) {

					Thread interventionsSaver = new Thread() {
						public void run() {
							frameParent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

							// save interventions to a new file
							File file = fileChooser.getSelectedFile();
							// add extension if missing
							if(FilenameUtils.getExtension(file.getName()).equals(""))
								file = new File(file.getAbsolutePath() + ".csv");

							try {
								FileWriter writer = new FileWriter(file);
								SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

								// header
								writer.write("Date,Calibration check,Calibration,Event,Incident,Comment\n");
								// body
								PriorityQueue<Intervention> interventions = new PriorityQueue<>(markerCalibration.size() + markerEvent.size() + markerIncident.size());
								Enumeration incidents = nodeIncident.children();
								while(incidents.hasMoreElements()) {
									Intervention i = ((InterventionNode) incidents.nextElement()).getIntervention();
									interventions.add(i);
								}
								Enumeration events = nodeEvent.children();
								while(events.hasMoreElements()) {
									Intervention i = ((InterventionNode) events.nextElement()).getIntervention();
									interventions.add(i);
								}
								Enumeration calibrations = nodeCalibration.children();
								while(calibrations.hasMoreElements()) {
									Intervention i = ((InterventionNode) calibrations.nextElement()).getIntervention();
									interventions.add(i);
								}
								while(!interventions.isEmpty()) {
									// date
									Intervention i = interventions.poll();
									writer.append(sdf.format(i.getDate())).append(",");
									// calibration check
									if(i.isCalibrationCheck())
										writer.append("1,");
									else
										writer.append(",");
									// calibration
									if(i.isCalibration())
										writer.append("1,");
									else
										writer.append(",");
									// event
									if(i.isEvent())
										writer.append("1,");
									else
										writer.append(",");
									// incident
									if(i.isIncident())
										writer.append("1,");
									else
										writer.append(",");
									// comment
									writer.append(i.getComment()).append("\n");
								}

								writer.flush();
								writer.close();

							} catch(IOException e1) {
								JOptionPane.showMessageDialog(frameParent, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							}

							frameParent.setCursor(Cursor.getDefaultCursor());
						}
					};
					interventionsSaver.start();
				}
			}
		}
	}

	private class ListenerAddIntervention implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// create intervention dialog
			InterventionDialog dialog = new InterventionDialog();

			int option = JOptionPane.showConfirmDialog(frameParent, dialog, "Add an intervention", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if(option == JOptionPane.OK_OPTION) {
				// create new intervention
				Intervention intervention;
				if(dialog.getComment().equals(""))
					intervention = new Intervention(dialog.getDate(), dialog.isCalibrationCheck(), dialog.isCalibration(), dialog.isEvent(), dialog.isIncident());
				else
					intervention = new Intervention(dialog.getDate(), dialog.isCalibrationCheck(), dialog.isCalibration(), dialog.isEvent(), dialog.isIncident(), dialog.getComment());

				// add to the interventions list and create a marker
				ValueMarker marker = null;
				boolean toDraw = false;
				if(intervention.isIncident()) {
					nodeIncident.add(new InterventionNode(intervention));
					sortInterventions(nodeIncident);

					marker = new ValueMarker(intervention.getDate().getTime(), Color.RED, new BasicStroke(1));
					markerIncident.put(intervention.getDate(), marker);
					toDraw = checkBoxIncident.isSelected();
				}
				else if(intervention.isEvent()) {
					nodeEvent.add(new InterventionNode(intervention));
					sortInterventions(nodeEvent);

					marker = new ValueMarker(intervention.getDate().getTime(), Color.BLUE, new BasicStroke(1));
					markerEvent.put(intervention.getDate(), marker);
					toDraw = checkBoxEvent.isSelected();
				}
				else if(intervention.isCalibration()) {
					nodeCalibration.add(new InterventionNode(intervention));
					sortInterventions(nodeCalibration);

					marker = new ValueMarker(intervention.getDate().getTime(), Color.GREEN, new BasicStroke(1));
					markerCalibration.put(intervention.getDate(), marker);
					toDraw = checkBoxCalibration.isSelected();
				}
				DefaultTreeModel treeModel = ((DefaultTreeModel) treeInterventions.getModel());
				treeModel.reload();

				// show all interventions in the interventions panel (TODO: might be reconsidered later on)
				expandInterventionsTree();
				// draw the interventions on the graph
				if(toDraw && chartPanel != null) {
					XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
					plot.addDomainMarker(marker);
				}
			}
		}
	}

	private class ListenerRemoveIntervention implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if(treeInterventions.getSelectionPath().getLastPathComponent() instanceof InterventionNode) {
				InterventionNode selectedNode = (InterventionNode) treeInterventions.getSelectionPath().getLastPathComponent();
				// remove from tree
				DefaultTreeModel treeModel = ((DefaultTreeModel) treeInterventions.getModel());
				treeModel.removeNodeFromParent(selectedNode);
				// remove from graph
				ValueMarker marker = removeMarker(selectedNode.getIntervention());
				if(chartPanel != null && marker != null) {
					XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
					plot.removeDomainMarker(marker);
				}
			}
		}
	}

	private class ListenerEditIntervention implements ActionListener {

		private Intervention intervention;

		public ListenerEditIntervention(Intervention i) {
			this.intervention = i;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// create intervention dialog
			InterventionDialog dialog = new InterventionDialog(intervention, false);

			int option = JOptionPane.showConfirmDialog(frameParent, dialog, "Edit intervention", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if(option == JOptionPane.OK_OPTION) {
				// retrieve the old marker
				ValueMarker marker = null;
				if(intervention.isIncident())
					marker = markerIncident.remove(intervention.getDate());
				else if(intervention.isEvent())
					marker = markerEvent.remove(intervention.getDate());
				else if(intervention.isCalibration())
					marker = markerCalibration.remove(intervention.getDate());

				// update the intervention
				intervention.setDate(dialog.getDate());
				if(!dialog.getComment().equals(""))
					intervention.setComment(dialog.getComment());

				// sort the interventions tree
				if(intervention.isIncident())
					sortInterventions(nodeIncident);
				else if(intervention.isEvent())
					sortInterventions(nodeEvent);
				else if(intervention.isCalibration())
					sortInterventions(nodeCalibration);
				DefaultTreeModel treeModel = ((DefaultTreeModel) treeInterventions.getModel());
				treeModel.reload();
				expandInterventionsTree();

				// update the marker
				if(marker != null) {
					marker.setValue(intervention.getDate().getTime());
					if(intervention.isIncident())
						markerIncident.put(intervention.getDate(), marker);
					else if(intervention.isEvent())
						markerEvent.put(intervention.getDate(), marker);
					else if(intervention.isCalibration())
						markerCalibration.put(intervention.getDate(), marker);
				}
			}
		}
	}

	private class ListenerClearInterventions implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			int option = JOptionPane.showConfirmDialog(frameParent, "Attention: this will remove all interventions.\nAny unsaved interventions will be lost!", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

			if(option == JOptionPane.OK_OPTION)
				clearInterventions();
		}
	}

	private class ListenerCheckBox implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(chartPanel != null) {
				XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();

				Object source = e.getItemSelectable();
				// show or hide the specific interventions
				if(source == checkBoxCalibration) {
					if(e.getStateChange() == ItemEvent.DESELECTED)
						markerCalibration.values().forEach(plot::removeDomainMarker);
					else
						markerCalibration.values().forEach(plot::addDomainMarker);
				}
				if(source == checkBoxEvent) {
					if(e.getStateChange() == ItemEvent.DESELECTED)
						markerEvent.values().forEach(plot::removeDomainMarker);
					else
						markerEvent.values().forEach(plot::addDomainMarker);
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
