package inspector.jmondb.viewer;

import inspector.jmondb.intervention.Intervention;
import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBReader;
import inspector.jmondb.model.Value;
import org.apache.commons.io.FilenameUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;

import javax.persistence.EntityManagerFactory;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
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
	private DefaultMutableTreeNode nodeInterventions;
	private DefaultMutableTreeNode nodeCalibration;
	private DefaultMutableTreeNode nodeEvent;
	private DefaultMutableTreeNode nodeIncident;
	private JTree treeInterventions;

	// connection to the iMonDB
	private EntityManagerFactory emf;
	private IMonDBReader dbReader;

	// list of interventions
	private HashMap<Date, Intervention> interventions;

	public static void main(String[] args) throws Exception {

		Viewer viewer = new Viewer();
		viewer.display();
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
		createInterventionsPanel(panelInterventions);

		interventions = new HashMap<>();
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
		JMenuItem menuItemAddIntervention = new JMenuItem("Add intervention");
		menuItemAddIntervention.addActionListener(new ListenerAddIntervention());
		menuFile.add(menuItemAddIntervention);
		JMenuItem menuItemRemoveIntervention = new JMenuItem("Remove intervention");
		menuItemRemoveIntervention.addActionListener(new ListenerRemoveIntervention());
		menuFile.add(menuItemRemoveIntervention);

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

		panelGraph = new JPanel();
		panelGraph.setBackground(Color.WHITE);
		panelGraph.setPreferredSize(new Dimension(1200, 750));
		panelParent.add(panelGraph, BorderLayout.CENTER);

		panelDbConnection.setBackground(Color.WHITE);
		panelParent.add(panelDbConnection, BorderLayout.PAGE_END);

		panelInterventions.setBackground(Color.WHITE);
		panelInterventions.setPreferredSize(new Dimension(300, 750));
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
		nodeInterventions = new DefaultMutableTreeNode("Interventions");
		treeInterventions = new JTree(nodeInterventions);
		JScrollPane scrollPaneInterventions = new JScrollPane(treeInterventions);
		scrollPaneInterventions.setPreferredSize(new Dimension(250, 750));

		interventionsPanel.add(scrollPaneInterventions);

		nodeCalibration = new DefaultMutableTreeNode("Calibration");
		nodeInterventions.add(nodeCalibration);
		nodeEvent = new DefaultMutableTreeNode("Event");
		nodeInterventions.add(nodeEvent);
		nodeIncident = new DefaultMutableTreeNode("Incident");
		nodeInterventions.add(nodeIncident);

		expandInterventionsTree();
	}

	public void display() {
		frameParent.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frameParent.setPreferredSize(new Dimension(1500, 900));
		frameParent.pack();
		frameParent.setVisible(true);
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
		for(int i = 0; i < treeInterventions.getRowCount(); i++) {
			treeInterventions.expandRow(i);
		}
	}

	private void drawInterventions() {
		if(chartPanel != null) {
			XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();

			Enumeration incidents = nodeIncident.children();
			while(incidents.hasMoreElements()) {
				Intervention i = ((InterventionNode)incidents.nextElement()).getIntervention();
				XYLineAnnotation annotation = new XYLineAnnotation(i.getDate().getTime(), plot.getRangeAxis().getLowerBound(),
						i.getDate().getTime(), plot.getRangeAxis().getUpperBound(), new BasicStroke(1), Color.RED);
				annotation.setToolTipText(i.getComment());
				plot.getRenderer().addAnnotation(annotation, Layer.BACKGROUND);
			}

			Enumeration events = nodeEvent.children();
			while(events.hasMoreElements()) {
				Intervention i = ((InterventionNode)events.nextElement()).getIntervention();
				XYLineAnnotation annotation = new XYLineAnnotation(i.getDate().getTime(), plot.getRangeAxis().getLowerBound(),
						i.getDate().getTime(), plot.getRangeAxis().getUpperBound(), new BasicStroke(1), Color.BLUE);
				annotation.setToolTipText(i.getComment());
				plot.getRenderer().addAnnotation(annotation, Layer.BACKGROUND);
			}

			Enumeration calibrations = nodeCalibration.children();
			while(calibrations.hasMoreElements()) {
				Intervention i = ((InterventionNode)calibrations.nextElement()).getIntervention();
				XYLineAnnotation annotation = new XYLineAnnotation(i.getDate().getTime(), plot.getRangeAxis().getLowerBound(),
						i.getDate().getTime(), plot.getRangeAxis().getUpperBound(), new BasicStroke(1), Color.RED);
				annotation.setToolTipText(i.getComment());
				plot.getRenderer().addAnnotation(annotation, Layer.FOREGROUND);
			}
		}
	}

	private void removeInterventions() {
		if(chartPanel != null)
			((XYPlot) chartPanel.getChart().getPlot()).getRenderer().removeAnnotations();
		interventions.clear();
	}

	private class ListenerConnectToDatabase implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			// create connection dialog
			DatabaseConnectionDialog connectionDialog = new DatabaseConnectionDialog();

			int option = JOptionPane.showConfirmDialog(frameParent, connectionDialog, "Connect to the database", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if(option == JOptionPane.OK_OPTION) {
				try {
					// first close an existing connection
					closeDbConnection();

					// create the new connection
					String password = !connectionDialog.getPassword().equals("") ? connectionDialog.getPassword() : null;
					emf = IMonDBManagerFactory.createMySQLFactory(connectionDialog.getHost(), connectionDialog.getPort(),
							connectionDialog.getUserName(), password, connectionDialog.getDatabase());
					dbReader = new IMonDBReader(emf);

					// show the connection information
					labelDbConnection.setText("Connected to " + connectionDialog.getUserName() + "@" + connectionDialog.getHost() + "/" + connectionDialog.getDatabase());
					labelDbIcon.setIcon(iconConnected);

					// fill in possible projects in the combo box
					List<String> projectLabels = dbReader.getFromCustomQuery("SELECT project.label FROM Project project ORDER BY project.label", String.class);
					projectLabels.forEach(comboBoxProject::addItem);
				}
				catch(Exception e1) {
					closeDbConnection();
					JOptionPane.showMessageDialog(frameParent, "<html><b>Could not connect to the database</b></html>\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
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

			if(dbReader != null) {
				comboBoxValue.removeAllItems();

				String projectLabel = (String)comboBoxProject.getSelectedItem();
				List<String> values = dbReader.getFromCustomQuery("SELECT DISTINCT val.name FROM Value val WHERE val.fromRun.fromProject.label = \"" + projectLabel + "\" ORDER BY val.name", String.class);
				values.forEach(comboBoxValue::addItem);
			}
		}
	}

	private class ListenerShowGraph implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			if(dbReader != null) {
				String projectLabel = (String)comboBoxProject.getSelectedItem();
				String valueName = (String)comboBoxValue.getSelectedItem();

				List<Value> values = dbReader.getFromCustomQuery("SELECT val FROM Value val WHERE val.fromRun.fromProject.label = \"" + projectLabel + "\" AND val.name = \"" + valueName + "\"", Value.class);

				if(values.size() == 0)
					JOptionPane.showMessageDialog(frameParent, "No matching values found.", "Warning", JOptionPane.WARNING_MESSAGE);
				if(values.size() > 0 && !values.get(0).getNumeric())
					JOptionPane.showMessageDialog(frameParent, "Value <" + valueName + "> is not numeric.", "Warning", JOptionPane.WARNING_MESSAGE);
				else {
					// add data
					XYSeries meanSeries = new XYSeries("Mean");
					XYSeries q1Series = new XYSeries("Q1");
					XYSeries q3Series = new XYSeries("Q3");
					XYSeries minSeries = new XYSeries("Min");
					XYSeries maxSeries = new XYSeries("Max");
					for(Value value : values) {
						meanSeries.add(value.getFromRun().getSampleDate().getTime(), value.getMean());
						q1Series.add(value.getFromRun().getSampleDate().getTime(), value.getQ1());
						q3Series.add(value.getFromRun().getSampleDate().getTime(), value.getQ3());
						minSeries.add(value.getFromRun().getSampleDate().getTime(), value.getMin());
						maxSeries.add(value.getFromRun().getSampleDate().getTime(), value.getMax());
					}
					XYSeriesCollection q1Collection = new XYSeriesCollection();
					q1Collection.addSeries(meanSeries);
					q1Collection.addSeries(q1Series);
					XYSeriesCollection q3Collection = new XYSeriesCollection();
					q3Collection.addSeries(meanSeries);
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
					DateTickUnit unit = new DateTickUnit(DateTickUnitType.DAY, 7);
					dateAxis.setTickUnit(unit);

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
					chart.setBackgroundPaint(java.awt.Color.WHITE);
					chartPanel = new ChartPanel(chart, false, true, false, true, false);
					chart.removeLegend();
					chartPanel.setPreferredSize(new Dimension(1200, 740));

					panelGraph.removeAll();
					panelGraph.add(chartPanel);
					panelGraph.validate();

					drawInterventions();
				}
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
				// remove previous interventions
				removeInterventions();

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
						if(intervention.isIncident())
							nodeIncident.add(node);
						else if(intervention.isEvent())
							nodeEvent.add(node);
						else if(intervention.isCalibration())
							nodeCalibration.add(node);
					}

				} catch(ParseException | IOException e1) {
					JOptionPane.showMessageDialog(frameParent, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}

				// show all interventions in the interventions panel (TODO: might be reconsidered later on)
				expandInterventionsTree();
				// draw the interventions on the graph
				drawInterventions();
			}
		}
	}

	private class ListenerAddIntervention implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

		}
	}

	private class ListenerRemoveIntervention implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

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
