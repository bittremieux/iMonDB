package inspector.imondb.viewer.controller.listeners;

import inspector.imondb.viewer.view.gui.JLabelLink;
import inspector.imondb.viewer.view.gui.ViewerFrame;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.util.concurrent.ExecutionException;

public class UpdateWorker extends SwingWorker<String, Void> {

    private static final String APP_ID = "viewer";
    private static final String VERSION_NR = UpdateWorker.class.getPackage().getImplementationVersion();

    private ViewerFrame viewerFrame;

    private boolean isSilent;

    public UpdateWorker(ViewerFrame viewerFrame, boolean isSilent) {
        this.viewerFrame = viewerFrame;
        this.isSilent = isSilent;
    }

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
            } else if(!isSilent) {
                // only show a message if the update was explicitly requested
                JOptionPane.showMessageDialog(viewerFrame.getFrame(), "No update found.",
                        "About", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch(InterruptedException | ExecutionException ex) {
            // only show a message if the update was explicitly requested
            if(!isSilent) {
                JOptionPane.showMessageDialog(viewerFrame.getFrame(), ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
