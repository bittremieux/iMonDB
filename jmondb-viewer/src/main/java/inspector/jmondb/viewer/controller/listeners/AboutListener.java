package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.view.gui.JLabelLink;
import inspector.jmondb.viewer.view.gui.ViewerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AboutListener implements ActionListener {

    private ViewerFrame viewerFrame;

    public AboutListener(ViewerFrame viewerFrame) {
        this.viewerFrame = viewerFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JLabelLink linkAbout = new JLabelLink("For more information, please visit our", "website",
                "https://bitbucket.org/proteinspector/jmondb", ".");
        JOptionPane.showMessageDialog(viewerFrame.getFrame(), linkAbout.getPanel(),
                "About", JOptionPane.INFORMATION_MESSAGE);
    }
}
