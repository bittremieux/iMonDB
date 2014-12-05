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
import java.awt.event.ItemEvent;

public class SettingsDialog extends JPanel {

	private JCheckBox checkBoxAutoConnect;
	private DatabaseConnectionDialog databaseDialog;

	public SettingsDialog() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		// load the settings
		loadDatabasePreferences();
	}

	private void loadDatabasePreferences() {
		// retrieve saved database settings
		checkBoxAutoConnect = new JCheckBox("<html>Automatically connect to the database.<br/>This will be applied the next time the application is launched.</html>");
		add(checkBoxAutoConnect);
		checkBoxAutoConnect.setAlignmentX(Component.CENTER_ALIGNMENT);
		checkBoxAutoConnect.setSelected(SettingsHandler.getSettings().getDatabaseAutoConnect());
		checkBoxAutoConnect.addItemListener(e -> {
			boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
			checkBoxAutoConnect.setSelected(enabled);
			databaseDialog.setEnabled(enabled);
		});

		String host = SettingsHandler.getSettings().getDatabaseHost();
		String port = SettingsHandler.getSettings().getDatabasePort();
		String username = SettingsHandler.getSettings().getDatabaseUserName();
		String password = SettingsHandler.getSettings().getDatabasePassword();
		String database = SettingsHandler.getSettings().getDatabaseName();

		databaseDialog = new DatabaseConnectionDialog(host, port, username, password, database);
		add(databaseDialog);
		databaseDialog.setMaximumSize(databaseDialog.getPreferredSize());
		databaseDialog.setEnabled(SettingsHandler.getSettings().getDatabaseAutoConnect());
	}

	public void saveDatabasePreferences() {
		SettingsHandler.getSettings().setDatabaseAutoConnect(checkBoxAutoConnect.isSelected());
		SettingsHandler.getSettings().setDatabaseHost(databaseDialog.getHost());
		SettingsHandler.getSettings().setDatabasePort(databaseDialog.getPort());
		SettingsHandler.getSettings().setDatabaseUserName(databaseDialog.getUserName());
		SettingsHandler.getSettings().setDatabasePassword(databaseDialog.getPassword());
		SettingsHandler.getSettings().setDatabaseName(databaseDialog.getDatabase());
	}
}
