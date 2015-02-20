package inspector.imondb.collector.controller;

import inspector.imondb.collector.model.config.Configuration;

import javax.swing.*;

public class ExecutionController {

    private DatabaseController databaseController;
    private Configuration configuration;

    public ExecutionController(DatabaseController databaseController, Configuration configuration) {
        this.databaseController = databaseController;
        this.configuration = configuration;
    }

    public CollectorTask getCollectorTask(JProgressBar progressBar) {
        return new CollectorTask(progressBar, databaseController, configuration);
    }
}
