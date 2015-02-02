package inspector.jmondb.viewer.view.gui;

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
