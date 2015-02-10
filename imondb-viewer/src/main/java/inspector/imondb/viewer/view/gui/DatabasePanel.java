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

public class DatabasePanel {

    private static ImageIcon iconNotConnected = new ImageIcon(ViewerFrame.class.getResource("/images/nok.png"), "not connected");
    private static ImageIcon iconConnected = new ImageIcon(ViewerFrame.class.getResource("/images/ok.png"), "connected");

    private JPanel panel;

    private JLabel labelDbConnection;
    private JLabel labelDbIcon;

    public DatabasePanel() {
        panel = new JPanel();

        panel.add(new JLabel("Database connection:"));

        labelDbConnection = new JLabel();
        panel.add(labelDbConnection);

        labelDbIcon = new JLabel();
        panel.add(labelDbIcon);

        setNotConnected();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setNotConnected() {
        labelDbConnection.setText("Not connected");
        labelDbIcon.setIcon(iconNotConnected);
    }

    public void setConnected(String host, String database, String userName) {
        labelDbConnection.setText("Connected to " + userName + "@" + host + "/" + database);
        labelDbIcon.setIcon(iconConnected);
    }
}
