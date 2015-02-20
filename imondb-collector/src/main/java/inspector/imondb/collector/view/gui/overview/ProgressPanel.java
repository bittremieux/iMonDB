package inspector.imondb.collector.view.gui.overview;

import inspector.imondb.collector.controller.CollectorTask;
import inspector.imondb.collector.controller.ExecutionController;
import inspector.imondb.collector.view.gui.CollectorFrame;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ProgressPanel {

    private JPanel panel;

    private JTextPane textPaneLog;
    private JProgressBar progressBar;

    private ExecutionPanel executionPanel;

    private CollectorFrame collectorFrame;
    private ExecutionController executionController;

    private CollectorTask task;

    public ProgressPanel(ExecutionPanel executionPanel, CollectorFrame collectorFrame, ExecutionController executionController) {
        this.executionPanel = executionPanel;
        this.collectorFrame = collectorFrame;
        this.executionController = executionController;

        textPaneLog.setEditable(false);
        textPaneLog.getDocument().addDocumentListener(new LimitLinesDocumentListener(50));
        redirectSystemStreams();

        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
    }

    // http://unserializableone.blogspot.com/2009/01/redirecting-systemout-and-systemerr-to.html
    private void redirectSystemStreams() {
        Style styleOut = textPaneLog.addStyle("out", null);
        StyleConstants.setForeground(styleOut, Color.BLACK);
        OutputStream osOut = new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
                updateTextPane(String.valueOf((char) b), styleOut);
            }
        };
        Style styleErr = textPaneLog.addStyle("err", styleOut);
        StyleConstants.setForeground(styleErr, Color.RED);
        OutputStream osErr = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextPane(String.valueOf((char) b), styleErr);
            }
        };

        System.setOut(new PrintStream(osOut, true));
        System.setErr(new PrintStream(osErr, true));
    }

    private void updateTextPane(final String text, Style style) {
        SwingUtilities.invokeLater(() -> {
            Document doc = textPaneLog.getDocument();
            try {
                doc.insertString(doc.getLength(), text, style);
            } catch(BadLocationException e) {
                throw new RuntimeException(e);
            }
            textPaneLog.setCaretPosition(doc.getLength());
        });
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
}
