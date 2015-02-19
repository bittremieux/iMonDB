package inspector.imondb.collector.view.update;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class UpdateWorker extends SwingWorker<String, Void> {

    private static final Logger LOGGER = LogManager.getLogger(UpdateWorker.class);

    private static final String VERSION_NR = UpdateWorker.class.getPackage().getImplementationVersion();

    private static final ImageIcon icon = new ImageIcon(UpdateWorker.class.getResource("/images/logo-64.png"));

    private final String id;
    private final String header;
    private final boolean isSilent;

    public UpdateWorker(String applicationId, String applicationName, boolean isSilent) {
        this.id = applicationId;
        this.header = "<html><b>" + applicationName + " v" + VERSION_NR + "</b></html>";
        this.isSilent = isSilent;
    }

    @Override
    protected String doInBackground() throws Exception {
        if(!isValidVersion(VERSION_NR)) {
            throw new IllegalArgumentException("Invalid version format");
        } else {
            int[] versionSplit = splitVersion(VERSION_NR);

            // read downloads web page
            Document doc = Jsoup.connect("https://bitbucket.org/proteinspector/imondb/downloads").get();

            // check if an updated version is available
            Elements downloads = doc.select("a.execute");
            for(Element download : downloads) {
                String href = download.attr("href");
                if(href.contains("downloads") && href.contains(id)) {
                    String version = href.substring(href.lastIndexOf('-') + 1, href.lastIndexOf('.'));
                    if(isValidVersion(version) && isMoreRecent(splitVersion(version), versionSplit)) {
                        return version;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void done() {
        try {
            String version = get();

            String title = "Update";
            if(version != null) {
                LOGGER.info("New update found. Current version: " + VERSION_NR + "; update version: " + version);
                JOptionPane.showMessageDialog(Frame.getFrames()[0], createUpdatePanel(version),
                        title, JOptionPane.INFORMATION_MESSAGE, icon);
            } else if(!isSilent) {
                LOGGER.info("No updates found. Current version: " + VERSION_NR);
                // only show a message if the update was explicitly requested
                JOptionPane.showMessageDialog(Frame.getFrames()[0], createNoUpdatePanel(),
                        title, JOptionPane.INFORMATION_MESSAGE, icon);
            }
        } catch(InterruptedException | ExecutionException ex) {
            if(!isSilent) {
                LOGGER.info("Unable to check for updates: ", ex);
                // only show an error message if the update was explicitly requested
                JOptionPane.showMessageDialog(Frame.getFrames()[0], createErrorUpdatePanel(ex.getMessage()),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean isValidVersion(String version) {
        return version != null && version.matches("^\\d\\.\\d\\.\\d$");
    }

    private int[] splitVersion(String version) {
        String[] splitStr = version.split("\\.");
        int[] splitInt = new int[splitStr.length];
        for(int i = 0; i < splitStr.length; i++) {
            splitInt[i] = Integer.parseInt(splitStr[i]);
        }

        return splitInt;
    }

    private boolean isMoreRecent(int[] versionNew, int[] versionOld) {
        if(versionNew.length != versionOld.length) {
            return false;
        } else {
            for(int i = 0; i < versionNew.length; i++) {
                if(versionNew[i] > versionOld[i]) {
                    return true;
                } else if(versionNew[i] < versionOld[i]) {
                    return false;
                }
            }
            return false;
        }
    }

    private JPanel createUpdatePanel(String version) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        headerPanel.add(new JLabel(header));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        messagePanel.add(headerPanel);

        JPanel versionPanel = new JPanel(new GridLayout(2, 2, 0, 0));
        versionPanel.add(new JLabel("Current version:"));
        versionPanel.add(new JLabel(VERSION_NR));
        versionPanel.add(new JLabel("New version:"));
        versionPanel.add(new JLabel(version));
        versionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        messagePanel.add(versionPanel);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        linkPanel.add(new JLabel("To update, please visit our "));
        linkPanel.add(new inspector.imondb.collector.view.about.JLabelLink("website", "https://bitbucket.org/proteinspector/imondb").getLabel());
        linkPanel.add(new JLabel("."));
        messagePanel.add(linkPanel);

        return messagePanel;
    }

    private JPanel createNoUpdatePanel() {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        headerPanel.add(new JLabel(header));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        messagePanel.add(headerPanel);

        JPanel versionPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        versionPanel.add(new JLabel("Current version: " + VERSION_NR));
        versionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        messagePanel.add(versionPanel);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        linkPanel.add(new JLabel("This version is up to date."));
        messagePanel.add(linkPanel);

        return messagePanel;
    }

    private JPanel createErrorUpdatePanel(String errorMessage) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        headerPanel.add(new JLabel(header));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        messagePanel.add(headerPanel);

        JPanel errorPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        errorPanel.add(new JLabel("Unable to update:"));
        errorPanel.add(new JLabel(errorMessage));
        messagePanel.add(errorPanel);

        return messagePanel;
    }
}
