package inspector.imondb.collector.view;

public interface ProgressReporter {

    public void setProgress(int progress);

    public void done();
}
