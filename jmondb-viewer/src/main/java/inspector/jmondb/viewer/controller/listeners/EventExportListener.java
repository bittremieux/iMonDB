package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.EventController;
import inspector.jmondb.viewer.model.DatabaseConnection;
import inspector.jmondb.viewer.view.gui.ViewerFrame;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class EventExportListener implements ActionListener {

    private ViewerFrame viewerFrame;

    private EventController eventController;

    public EventExportListener(ViewerFrame frame, EventController eventController) {
        this.viewerFrame = frame;
        this.eventController = eventController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!DatabaseConnection.getConnection().isActive()) {
            JOptionPane.showMessageDialog(viewerFrame.getFrame(),
                    "Please connect to a database to export an event log.", "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("PDF documents", "pdf"));

                int returnVal = fileChooser.showSaveDialog(viewerFrame.getFrame());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    final File file = FilenameUtils.getExtension(fileChooser.getSelectedFile().getName()).equals("") ?
                            new File(fileChooser.getSelectedFile().getAbsolutePath() + ".pdf") :
                            fileChooser.getSelectedFile();

                    Thread eventExporter = new Thread() {
                        public void run() {
                            eventController.exportEvents(file);
                        }
                    };
                    eventExporter.start();
                }
            } catch(IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(viewerFrame.getFrame(), ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            } catch(IllegalStateException ex) {
                JOptionPane.showMessageDialog(viewerFrame.getFrame(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
