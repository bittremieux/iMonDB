package inspector.jmondb.viewer.model;

/*
 * #%L
 * jMonDB Viewer
 * %%
 * Copyright (C) 2014 - 2015 InSPECtor
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

import java.util.Map;

public class DatabaseConfiguration extends Configuration {

    private static final boolean CONNECT_DEFAULT = false;

    private final Map<String, String> DATABASE_DEFAULTS = ImmutableMap.of(
            "db_host", "localhost",
            "db_port", "3306",
            "db_username", "",
            "db_password", "",
            "db_database", "iMonDB"
    );

    public DatabaseConfiguration() {
        super();
    }

    public boolean getAutoConnect() {
        return preferences.getBoolean("db_autoconnect", CONNECT_DEFAULT);
    }

    public void setAutoConnect(boolean autoConnect) {
        preferences.putBoolean("db_autoconnect", autoConnect);
    }

    public String getHost() {
        String key = "db_host";
        return preferences.get(key, DATABASE_DEFAULTS.get(key));
    }

    public void setHost(String host) {
        String key = "db_host";
        preferences.put(key, host);
    }

    public String getPort() {
        String key = "db_port";
        return preferences.get(key, DATABASE_DEFAULTS.get(key));
    }

    public void setPort(String port) {
        String key = "db_port";
        preferences.put(key, port);
    }

    public String getUserName() {
        String key = "db_username";
        return preferences.get(key, DATABASE_DEFAULTS.get(key));
    }

    public void setUserName(String username) {
        String key = "db_username";
        preferences.put(key, username);
    }

    public String getPassword() {
        String key = "db_password";
        // not so pretty because of not-null constraint of the map
        return preferences.get(key, null);
    }

    public void setPassword(String password) {
        String key = "db_password";
        if(password != null) {
            preferences.put(key, password);
        } else {
            preferences.remove(key);
        }
    }

    public String getDatabase() {
        String key = "db_database";
        return preferences.get(key, DATABASE_DEFAULTS.get(key));
    }

    public void setDatabase(String database) {
        String key = "db_database";
        preferences.put(key, database);
    }

    public void reset() {
        preferences.putBoolean("db_autoconnect", CONNECT_DEFAULT);

        for(Map.Entry<String, String> entry : DATABASE_DEFAULTS.entrySet()) {
            preferences.put(entry.getKey(), entry.getValue());
        }
    }
}
