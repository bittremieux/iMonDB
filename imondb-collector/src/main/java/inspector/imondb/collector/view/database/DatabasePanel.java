package inspector.imondb.collector.view.database;

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

import inspector.imondb.collector.controller.listeners.ConfigurationChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;
import java.util.EventListener;

public class DatabasePanel {

    public enum ConnectionStatus {
        CONNECTED, FAILED_CONNECTION, IN_PROGRESS, UNKNOWN
    }

    private static ImageIcon iconInvalidConnection = new ImageIcon(DatabasePanel.class.getResource("/images/nok.png"));
    private static ImageIcon iconUnknownConnection = new ImageIcon(DatabasePanel.class.getResource("/images/warning.png"));
    private static ImageIcon iconValidConnection = new ImageIcon(DatabasePanel.class.getResource("/images/ok.png"));

    private JPanel panel;

    private JButton buttonVerify;
    private JLabel labelStatus;

    private JTextField textFieldHost;
    private JTextField textFieldPort;
    private JTextField textFieldDatabase;
    private JTextField textFieldUserName;
    private JPasswordField passwordField;

    private ConnectionStatus connectionStatus;

    public DatabasePanel() {
        connectionStatus = ConnectionStatus.UNKNOWN;

        textFieldHost.setName("db_host");
        textFieldPort.setName("db_port");
        textFieldDatabase.setName("db_database");
        textFieldUserName.setName("db_username");
        passwordField.setName("db_password");
    }

    public DatabasePanel(String host, String port, String username, String password, String database) {
        this();

        textFieldHost.setText(host);
        textFieldPort.setText(port);
        textFieldDatabase.setText(database);
        textFieldUserName.setText(username);
        passwordField.setText(password);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getHost() {
        return textFieldHost.getText();
    }

    public String getPort() {
        return textFieldPort.getText();
    }

    public String getDatabase() {
        return textFieldDatabase.getText();
    }

    public String getUserName() {
        return textFieldUserName.getText();
    }

    public String getPassword() {
        String password = new String(passwordField.getPassword());
        return password.isEmpty() ? null : password;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatus status) {
        connectionStatus = status;

        switch(connectionStatus) {
            case CONNECTED:
                labelStatus.setIcon(iconValidConnection);
                labelStatus.setText("Valid connection settings");
                break;
            case FAILED_CONNECTION:
                labelStatus.setIcon(iconInvalidConnection);
                labelStatus.setText("Invalid connection settings");
                break;
            case IN_PROGRESS:
                labelStatus.setIcon(iconUnknownConnection);
                labelStatus.setText("Verifying connection settings");
                break;
            case UNKNOWN:
            default:
                labelStatus.setIcon(iconUnknownConnection);
                labelStatus.setText("No/unverified connection");
                break;
        }

        if(connectionStatus == ConnectionStatus.IN_PROGRESS) {
            textFieldHost.setEnabled(false);
            textFieldPort.setEnabled(false);
            textFieldDatabase.setEnabled(false);
            textFieldUserName.setEnabled(false);
            passwordField.setEnabled(false);
        } else {
            textFieldHost.setEnabled(true);
            textFieldPort.setEnabled(true);
            textFieldDatabase.setEnabled(true);
            textFieldUserName.setEnabled(true);
            passwordField.setEnabled(true);
        }
    }

    public void addConfigurationChangeListener(ConfigurationChangeListener listener) {
        textFieldHost.addFocusListener(listener);
        textFieldPort.addFocusListener(listener);
        textFieldDatabase.addFocusListener(listener);
        textFieldUserName.addFocusListener(listener);
        passwordField.addFocusListener(listener);
    }

    public void addDatabaseConnectionListener(EventListener listener) {
        buttonVerify.addActionListener((ActionListener) listener);

        textFieldHost.getDocument().addDocumentListener((DocumentListener) listener);
        textFieldPort.getDocument().addDocumentListener((DocumentListener) listener);
        textFieldDatabase.getDocument().addDocumentListener((DocumentListener) listener);
        textFieldUserName.getDocument().addDocumentListener((DocumentListener) listener);
        passwordField.getDocument().addDocumentListener((DocumentListener) listener);
    }
}
