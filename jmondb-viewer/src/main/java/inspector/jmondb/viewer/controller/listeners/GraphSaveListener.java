package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GraphSaveListener implements ActionListener {

    private ViewerFrame viewerFrame;

    public GraphSaveListener(ViewerFrame viewerFrame) {
        this.viewerFrame = viewerFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(viewerFrame.getGraphPanel().getPanel().getChart() != null) {
            Thread graphSaver = new Thread() {
                @Override
                public void run() {
                    try {
                        viewerFrame.getGraphPanel().getPanel().doSaveAs();
                    } catch(IOException ex) {
                        JOptionPane.showMessageDialog(viewerFrame.getFrame(),
                                "<html><b>Could not save the graph</b></html>\n" + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            graphSaver.start();
        } else {
            JOptionPane.showMessageDialog(viewerFrame.getFrame(), "No graph available.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
}
