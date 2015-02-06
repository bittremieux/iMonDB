package inspector.jmondb.viewer.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class DatabaseConfiguration extends Configuration {

    private static final boolean CONNECT_DEFAULT = false;

    private final Map<String, String> DATABASE_DEFAULTS = ImmutableMap.of(
            "db.host", "localhost",
            "db.port", "3306",
            "db.username", "",
            "db.password", "",
            "db.database", "iMonDB"
    );

    public DatabaseConfiguration() {
        super();
    }

    public boolean getAutoConnect() {
        return PREFERENCES.getBoolean("db.autoconnect", CONNECT_DEFAULT);
    }

    public void setAutoConnect(boolean autoConnect) {
        PREFERENCES.putBoolean("db.autoconnect", autoConnect);
    }

    public String getHost() {
        String key = "db.host";
        return PREFERENCES.get(key, DATABASE_DEFAULTS.get(key));
    }

    public void setHost(String host) {
        String key = "db.host";
        PREFERENCES.put(key, host);
    }

    public String getPort() {
        String key = "db.port";
        return PREFERENCES.get(key, DATABASE_DEFAULTS.get(key));
    }

    public void setPort(String port) {
        String key = "db.port";
        PREFERENCES.put(key, port);
    }

    public String getUserName() {
        String key = "db.username";
        return PREFERENCES.get(key, DATABASE_DEFAULTS.get(key));
    }

    public void setUserName(String username) {
        String key = "db.username";
        PREFERENCES.put(key, username);
    }

    public String getPassword() {
        String key = "db.password";
        // not so pretty because of not-null constraint of the map
        return PREFERENCES.get(key, null);
    }

    public void setPassword(String password) {
        String key = "db.password";
        if(password != null) {
            PREFERENCES.put(key, password);
        } else {
            PREFERENCES.remove(key);
        }
    }

    public String getDatabase() {
        String key = "db.database";
        return PREFERENCES.get(key, DATABASE_DEFAULTS.get(key));
    }

    public void setDatabase(String database) {
        String key = "db.database";
        PREFERENCES.put(key, database);
    }

    public void reset() {
        PREFERENCES.putBoolean("db.autoconnect", CONNECT_DEFAULT);

        for(Map.Entry<String, String> entry : DATABASE_DEFAULTS.entrySet()) {
            PREFERENCES.put(entry.getKey(), entry.getValue());
        }
    }
}
