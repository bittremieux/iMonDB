package inspector.imondb.collector.view.overview;

import inspector.imondb.collector.controller.CollectorTask;
import inspector.imondb.collector.controller.ExecutionController;
import inspector.imondb.collector.view.CollectorFrame;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ProgressPanel {

    private JPanel panel;

    private JTextArea textAreaLog;
    private JProgressBar progressBar;

    private ExecutionPanel executionPanel;

    private CollectorFrame collectorFrame;
    private ExecutionController executionController;

    private CollectorTask task;

    public ProgressPanel(ExecutionPanel executionPanel, CollectorFrame collectorFrame, ExecutionController executionController) {
        this.executionPanel = executionPanel;
        this.collectorFrame = collectorFrame;
        this.executionController = executionController;

        textAreaLog.setEditable(false);
        redirectSystemStreams();

        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void start() {
        collectorFrame.setWaitCursor(true);
        reset();

        task = executionController.getCollectorTask(this);
        task.execute();
    }

    private void reset() {
        setProgress(0);
    }

    public void stop() {
        task.cancelExecution();

        collectorFrame.setWaitCursor(false);
    }

    public void setProgress(int percentage) {
        progressBar.setValue(percentage);
        progressBar.setString(percentage + " %");
    }

    public void done() {
        executionPanel.getButton().setText("Start collector");
        collectorFrame.setWaitCursor(false);
    }

    // http://unserializableone.blogspot.com/2009/01/redirecting-systemout-and-systemerr-to.html
    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() -> textAreaLog.append(text));
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}
