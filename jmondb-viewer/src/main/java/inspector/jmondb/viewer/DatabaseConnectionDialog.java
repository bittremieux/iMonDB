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

import javax.swing.*;
import java.awt.*;

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

	public DatabaseConnectionDialog(String host, String port, String username, String password, String database) {
		this();

		textFieldHost.setText(host);
		textFieldPort.setText(port);
		textFieldUser.setText(username);
		passwordField.setText(password);
		textFieldDatabase.setText(database);
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

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.setEnabledRecursive(this, enabled);
	}

	protected void setEnabledRecursive(Component component, boolean enabled) {
		if(component instanceof Container) {
			for(Component child : ((Container) component).getComponents()) {
				child.setEnabled(enabled);
			}
		}
	}
}
