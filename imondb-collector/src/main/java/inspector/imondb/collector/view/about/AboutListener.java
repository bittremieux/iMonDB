package inspector.imondb.collector.view.about;

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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AboutListener implements ActionListener {

    private static final ImageIcon icon = new ImageIcon(AboutListener.class.getResource("/images/logo-64.png"));

    private final String applicationName;

    public AboutListener(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String title = "About";

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        headerPanel.add(new JLabel("<html><b>" + applicationName + " v" +
                getClass().getPackage().getImplementationVersion() + "</b></html>"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        messagePanel.add(headerPanel);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        linkPanel.add(new JLabel("For more information, please visit our "));
        linkPanel.add(new JLabelLink("website", "https://bitbucket.org/proteinspector/imondb").getLabel());
        linkPanel.add(new JLabel("."));
        messagePanel.add(linkPanel);

        JOptionPane.showMessageDialog(Frame.getFrames()[0], messagePanel, title, JOptionPane.INFORMATION_MESSAGE, icon);
    }
}
