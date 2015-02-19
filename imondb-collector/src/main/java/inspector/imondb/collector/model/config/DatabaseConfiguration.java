package inspector.imondb.collector.model.config;

/*
 * #%L
 * iMonDB Collector
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
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.Map;

public class DatabaseConfiguration {

    private static final Map<String, String> DATABASE_DEFAULTS = ImmutableMap.of(
            "db_host", "localhost",
            "db_port", "3306",
            "db_username", "",
            "db_password", "",
            "db_database", "iMonDB"
    );

    private Map<String, Object> rootMap;

    private BasicTextEncryptor textEncryptor;

    public DatabaseConfiguration(Map<String, Object> map) {
        rootMap = map;

        textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword("iMonDB Collector not so secret encryption password");
    }

    public String getHost() {
        @SuppressWarnings("unchecked")
        String result = ((Map<String, String>) rootMap.get("sql")).get("host");
        return result != null ? result : DATABASE_DEFAULTS.get("db_host");
    }

    public void setHost(String host) {
        @SuppressWarnings("unchecked")
        Map<String, String> sqlMap = (Map<String, String>) rootMap.get("sql");
        sqlMap.put("host", host != null && !host.isEmpty() ? host : DATABASE_DEFAULTS.get("db_host"));
    }

    public String getPort() {
        @SuppressWarnings("unchecked")
        String result = ((Map<String, String>) rootMap.get("sql")).get("port");
        return result != null ? result : DATABASE_DEFAULTS.get("db_port");
    }

    public void setPort(String port) {
        @SuppressWarnings("unchecked")
        Map<String, String> sqlMap = (Map<String, String>) rootMap.get("sql");
        sqlMap.put("port", port != null && !port.isEmpty() ? port : DATABASE_DEFAULTS.get("db_port"));
    }

    public String getDatabase() {
        @SuppressWarnings("unchecked")
        String result = ((Map<String, String>) rootMap.get("sql")).get("database");
        return result != null ? result : DATABASE_DEFAULTS.get("db_database");
    }

    public void setDatabase(String database) {
        @SuppressWarnings("unchecked")
        Map<String, String> sqlMap = (Map<String, String>) rootMap.get("sql");
        sqlMap.put("database", database != null && !database.isEmpty() ? database : DATABASE_DEFAULTS.get("db_database"));
    }

    public String getUserName() {
        @SuppressWarnings("unchecked")
        String result = ((Map<String, String>) rootMap.get("sql")).get("user");
        return result != null ? result : DATABASE_DEFAULTS.get("db_username");
    }

    public void setUserName(String username) {
        @SuppressWarnings("unchecked")
        Map<String, String> sqlMap = (Map<String, String>) rootMap.get("sql");
        sqlMap.put("user", username != null && !username.isEmpty() ? username : DATABASE_DEFAULTS.get("db_username"));
    }

    public String getPassword() {
        @SuppressWarnings("unchecked")
        String result = ((Map<String, String>) rootMap.get("sql")).get("password");
        return textEncryptor.decrypt(result);
    }

    public void setPassword(String password) {
        @SuppressWarnings("unchecked")
        Map<String, String> sqlMap = (Map<String, String>) rootMap.get("sql");
        sqlMap.put("password", textEncryptor.encrypt(password));
    }
}
