package inspector.jmondb.viewer;

import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBReader;
import inspector.jmondb.model.Value;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.persistence.EntityManagerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

	// connection to the iMonDB
	private EntityManagerFactory emf;
	private IMonDBReader dbReader;

	public static void main(String[] args) throws Exception {

		Viewer viewer = new Viewer();
		viewer.display();
	}

	public Viewer() {
		frameParent = new JFrame("iMonDB Viewer");
		frameParent.setBackground(Color.WHITE);

		JPanel panelParent = new JPanel(new GridBagLayout());
		panelParent.setBackground(Color.WHITE);
		frameParent.setContentPane(panelParent);

		// create menu bar
		frameParent.setJMenuBar(createMenuBar());

		// arrange panels
		JPanel panelSelection = new JPanel();
		JPanel panelDbConnection = new JPanel();
		arrangePanels(panelParent, panelSelection, panelDbConnection);

		// value selection panel
		createSelectionPanel(panelSelection);

		// database connection panel
		createDbConnectionPanel(panelDbConnection);
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
		//TODO
		menuItemAbout.addActionListener(null);
		menuHelp.add(menuItemAbout);

		// add to menu bar
		menuBar.add(menuHelp);

		return menuBar;
	}

	private void arrangePanels(JPanel panelParent, JPanel panelSelection, JPanel panelDbConnection) {
		GridBagConstraints constraints;
		panelSelection.setBackground(Color.WHITE);
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.PAGE_START;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelParent.add(panelSelection, constraints);

		panelGraph = new JPanel();
		panelGraph.setBackground(Color.WHITE);
		panelGraph.setPreferredSize(new Dimension(1200, 750));
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weighty = 1;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelParent.add(panelGraph, constraints);

		panelDbConnection.setBackground(Color.WHITE);
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.anchor = GridBagConstraints.PAGE_END;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelParent.add(panelDbConnection, constraints);
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

	public void display() {
		frameParent.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frameParent.setPreferredSize(new Dimension(1200, 900));
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
					XYSeriesCollection dataset = new XYSeriesCollection();
					XYSeries meanSeries = new XYSeries("Mean");
					XYSeries minSeries = new XYSeries("Min");
					XYSeries maxSeries = new XYSeries("Max");
					XYSeries q1Series = new XYSeries("Q1");
					XYSeries q3Series = new XYSeries("Q3");
					for(Value value : values) {
						meanSeries.add(value.getFromRun().getSampleDate().getTime(), value.getMean());
						minSeries.add(value.getFromRun().getSampleDate().getTime(), value.getMin());
						maxSeries.add(value.getFromRun().getSampleDate().getTime(), value.getMax());
						q1Series.add(value.getFromRun().getSampleDate().getTime(), value.getQ1());
						q3Series.add(value.getFromRun().getSampleDate().getTime(), value.getQ3());
					}
					dataset.addSeries(meanSeries);
					dataset.addSeries(minSeries);
					dataset.addSeries(maxSeries);
					dataset.addSeries(q1Series);
					dataset.addSeries(q3Series);

					// create axis
					DateAxis dateAxis = new DateAxis("Date");
					dateAxis.setDateFormatOverride(new SimpleDateFormat("dd/MM/yyyy"));
					DateTickUnit unit = new DateTickUnit(DateTickUnitType.DAY, 7);
					dateAxis.setTickUnit(unit);

					NumberAxis valueAxis = new NumberAxis("Value");
					valueAxis.setAutoRangeIncludesZero(false);

					// renderer
					XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);

					// create plot and draw graph
					XYPlot plot = new XYPlot(dataset, dateAxis, valueAxis, renderer);
					JFreeChart chart = new JFreeChart(valueName, plot);
					chart.setBackgroundPaint(java.awt.Color.WHITE);
					chartPanel = new ChartPanel(chart, false, true, false, true, false);
					chartPanel.setPreferredSize(new Dimension(1200, 740));

					panelGraph.removeAll();
					panelGraph.add(chartPanel);
					panelGraph.validate();
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

	private class ListenerQuit implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			closeDbConnection();
			System.exit(0);
		}
	}
}
