package inspector.imondb.collector.view.cli;

import inspector.imondb.collector.view.ProgressReporter;
import org.apache.commons.lang.StringUtils;

public class SystemOutProgressBar implements ProgressReporter {

    @Override
    public void setProgress(int progress) {
        System.out.print("[" + StringUtils.repeat("=", progress / 2) + StringUtils.repeat(" ", 50 - progress / 2) + "] " + progress + " %\r");
    }

    @Override
    public void done() {
        System.out.println("Completed");
    }
}
