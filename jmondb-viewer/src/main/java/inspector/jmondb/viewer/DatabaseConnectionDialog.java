package inspector.jmondb.viewer;

import javax.swing.*;

public class DatabaseConnectionDialog extends JPanel {

	private JTextField textFieldHost;
	private JTextField textFieldPort;
	private JTextField textFieldUser;
	private JPasswordField passwordField;
	private JTextField textFieldDatabase;

	public DatabaseConnectionDialog() {
		setLayout(new SpringLayout());

		JLabel labelHost = new JLabel("Host: ", JLabel.TRAILING);
		add(labelHost);
		textFieldHost = new JTextField("localhost");
		labelHost.setLabelFor(textFieldHost);
		add(textFieldHost);

		JLabel labelPort = new JLabel("Port: ", JLabel.TRAILING);
		add(labelPort);
		textFieldPort = new JTextField("3306");
		labelPort.setLabelFor(textFieldPort);
		add(textFieldPort);

		JLabel labelUser = new JLabel("User name: ", JLabel.TRAILING);
		add(labelUser);
		textFieldUser = new JTextField(15);
		labelUser.setLabelFor(textFieldUser);
		add(textFieldUser);

		JLabel labelPass = new JLabel("Password: ", JLabel.TRAILING);
		add(labelPass);
		passwordField = new JPasswordField();
		labelPass.setLabelFor(passwordField);
		add(passwordField);

		JLabel labelDatabase = new JLabel("Database: ", JLabel.TRAILING);
		add(labelDatabase);
		textFieldDatabase = new JTextField("iMonDB");
		labelDatabase.setLabelFor(textFieldDatabase);
		add(textFieldDatabase);

		SpringUtilities.makeCompactGrid(this, 5, 2, 6, 6, 6, 6);
	}

	public String getHost() {
		return textFieldHost.getText();
	}

	public String getPort() {
		return textFieldPort.getText();
	}

	public String getUserName() {
		return textFieldUser.getText();
	}

	public String getPassword() {
		return new String(passwordField.getPassword());
	}

	public String getDatabase() {
		return textFieldDatabase.getText();
	}
}
