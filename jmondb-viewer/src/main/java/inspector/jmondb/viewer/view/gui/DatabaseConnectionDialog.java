package inspector.jmondb.viewer.view.gui;

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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DatabaseConnectionDialog {

    private JDialog dialog;

    private boolean cancel;

    public DatabaseConnectionDialog(Frame owner, String host, String database, String userName) {
        cancel = false;

        dialog = new JDialog(owner, "Connecting", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 125);
        dialog.setLocationRelativeTo(owner);

        JLabel label = new JLabel("Connecting to " + userName + "@" + host + "/" + database + " ...");
        dialog.add(label, BorderLayout.PAGE_START);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        dialog.add(progressBar, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(new CancelListener());
        buttonPanel.add(buttonCancel);
        dialog.add(buttonPanel, BorderLayout.PAGE_END);
    }

    public boolean showDialog() {
        dialog.setVisible(true);
        return cancel;
    }

    public void hideDialog(boolean isCancelled) {
        this.cancel = isCancelled;
        dialog.dispose();
    }

    class CancelListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
            cancel = true;
        }
    }
}
