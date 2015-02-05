package inspector.jmondb.viewer.controller.listeners;

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

import inspector.jmondb.viewer.view.gui.JLabelLink;
import inspector.jmondb.viewer.view.gui.ViewerFrame;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

public class UpdateListener implements ActionListener {

    private static final String APP_ID = "viewer";
    private static final String VERSION_NR = UpdateListener.class.getPackage().getImplementationVersion();

    private ViewerFrame viewerFrame;

    public UpdateListener(ViewerFrame viewerFrame) {
        this.viewerFrame = viewerFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                if(isValidVersion(VERSION_NR)) {
                    int[] versionSplit = splitVersionToInt(VERSION_NR);

                    // read downloads web page
                    Document doc = Jsoup.connect("https://bitbucket.org/proteinspector/imondb/downloads").get();

                    // check if an updated version is available
                    Elements downloads = doc.select("a.execute");
                    for(Element download : downloads) {
                        String href = download.attr("href");
                        if(href.contains("downloads") && href.contains(APP_ID)) {
                            String version = href.substring(href.lastIndexOf('-') + 1, href.lastIndexOf('.'));
                            if(isValidVersion(version) && isMoreRecent(splitVersionToInt(version), versionSplit)) {
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

                    if(version != null) {
                        JLabelLink linkUpdate = new JLabelLink("Update found: iMonDB Viewer v" + version +
                                "<br><br>To update to the latest version, please visit our ",
                                "website", "https://bitbucket.org/proteinspector/imondb", ".");

                        JOptionPane.showMessageDialog(viewerFrame.getFrame(), linkUpdate.getLabel(),
                                "About", JOptionPane.INFORMATION_MESSAGE);
                    } else if(e.getActionCommand().equals("Check for updates")) {
                        // only show a message if the update was explicitly requested
                        JOptionPane.showMessageDialog(viewerFrame.getFrame(), "No update found.",
                                "About", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch(InterruptedException | ExecutionException ex) {
                    // only show a message if the update was explicitly requested
                    if(e.getActionCommand().equals("Check for updates")) {
                        JOptionPane.showMessageDialog(viewerFrame.getFrame(), ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        worker.execute();
    }

    private boolean isValidVersion(String version) {
        return version != null && version.matches("^\\d\\.\\d\\.\\d$");
    }

    private int[] splitVersionToInt(String version) {
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
}
