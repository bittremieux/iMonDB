package inspector.imondb.viewer.view.gui;

/*
 * #%L
 * iMonDB Viewer
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

import javax.swing.*;
import java.awt.*;

public class DatabaseConnectionPanel {

    private JPanel panel;

    private JTextField fieldHost;
    private JTextField fieldPort;
    private JTextField fieldUser;
    private JPasswordField fieldPassword;
    private JTextField fieldDatabase;

    public DatabaseConnectionPanel() {
        panel = new JPanel(new SpringLayout());

        JLabel labelHost = new JLabel("Host: ", JLabel.TRAILING);
        panel.add(labelHost);
        fieldHost = new JTextField("localhost");
        labelHost.setLabelFor(fieldHost);
        panel.add(fieldHost);

        JLabel labelPort = new JLabel("Port: ", JLabel.TRAILING);
        panel.add(labelPort);
        fieldPort = new JTextField("3306");
        labelPort.setLabelFor(fieldPort);
        panel.add(fieldPort);

        JLabel labelUser = new JLabel("User name: ", JLabel.TRAILING);
        panel.add(labelUser);
        fieldUser = new JTextField(15);
        labelUser.setLabelFor(fieldUser);
        panel.add(fieldUser);

        JLabel labelPass = new JLabel("Password: ", JLabel.TRAILING);
        panel.add(labelPass);
        fieldPassword = new JPasswordField();
        labelPass.setLabelFor(fieldPassword);
        panel.add(fieldPassword);

        JLabel labelDatabase = new JLabel("Database: ", JLabel.TRAILING);
        panel.add(labelDatabase);
        fieldDatabase = new JTextField("iMonDB");
        labelDatabase.setLabelFor(fieldDatabase);
        panel.add(fieldDatabase);

        SpringUtilities.makeCompactGrid(panel, 5, 2, 6, 6, 6, 6);
    }

    public DatabaseConnectionPanel(String host, String port, String username, String password, String database) {
        this();

        fieldHost.setText(host);
        fieldPort.setText(port);
        fieldUser.setText(username);
        fieldPassword.setText(password);
        fieldDatabase.setText(database);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getHost() {
        return fieldHost.getText();
    }

    public String getPort() {
        return fieldPort.getText();
    }

    public String getUserName() {
        return fieldUser.getText();
    }

    public String getPassword() {
        String password = new String(fieldPassword.getPassword());
        return password.isEmpty() ? null : password;
    }

    public String getDatabase() {
        return fieldDatabase.getText();
    }

    public void setEnabled(boolean enabled) {
        panel.setEnabled(enabled);
        this.setEnabledRecursive(panel, enabled);
    }

    protected void setEnabledRecursive(Component component, boolean enabled) {
        if(component instanceof Container) {
            for(Component child : ((Container) component).getComponents()) {
                child.setEnabled(enabled);
            }
        }
    }
}
