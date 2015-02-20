package inspector.imondb.collector.view.overview;

import javax.swing.*;

public class ProgressPanel {

    private JPanel panel;

    private JTextArea textAreaLog;
    private JProgressBar progressBar;

    public ProgressPanel() {
        progressBar.setMaximum(100);
    }

    public JPanel getPanel() {
        return panel;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }
}
