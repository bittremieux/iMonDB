package inspector.imondb.collector.controller;

import inspector.imondb.collector.model.config.Configuration;
import inspector.imondb.collector.view.gui.overview.ProgressPanel;

public class ExecutionController {

    private DatabaseController databaseController;
    private Configuration configuration;

    public ExecutionController(DatabaseController databaseController, Configuration configuration) {
        this.databaseController = databaseController;
        this.configuration = configuration;
    }

    public CollectorTask getCollectorTask(ProgressPanel progressPanel) {
        return new CollectorTask(progressPanel, databaseController, configuration);
    }
}
