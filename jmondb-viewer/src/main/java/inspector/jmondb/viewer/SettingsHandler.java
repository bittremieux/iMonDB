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

import java.util.prefs.Preferences;

public class SettingsHandler {

	private static final SettingsHandler settings = new SettingsHandler();

	private Preferences preferences;

	private SettingsHandler() {
		preferences = Preferences.userNodeForPackage(SettingsHandler.class);
	}

	public static SettingsHandler getSettings() {
		return settings;
	}

	public boolean getDatabaseAutoConnect() {
		return preferences.getBoolean("autoConnect", false);
	}

	public void setDatabaseAutoConnect(boolean autoConnect) {
		preferences.putBoolean("autoConnect", autoConnect);
	}

	public String getDatabaseHost() {
		return preferences.get("host", "localhost");
	}

	public void setDatabaseHost(String host) {
		preferences.put("host", host);
	}

	public String getDatabasePort() {
		return preferences.get("port", "3306");
	}

	public void setDatabasePort(String port) {
		preferences.put("port", port);
	}

	public String getDatabaseUserName() {
		return preferences.get("username", null);
	}

	public void setDatabaseUserName(String userName) {
		preferences.put("username", userName);
	}

	public String getDatabasePassword() {
		return preferences.get("password", null);
	}

	public void setDatabasePassword(String password) {
		preferences.put("password", password);
	}

	public String getDatabaseName() {
		return preferences.get("database", "iMonDB");
	}

	public void setDatabaseName(String database) {
		preferences.put("database", database);
	}
}
