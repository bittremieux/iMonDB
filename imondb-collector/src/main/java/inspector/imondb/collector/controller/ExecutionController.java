package inspector.imondb.collector.controller;

import inspector.imondb.collector.model.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class ExecutionController {

    private static final Logger LOGGER = LogManager.getLogger(ExecutionController.class);

    private DatabaseController databaseController;
    private Configuration configuration;

    public ExecutionController(DatabaseController databaseController, Configuration configuration) {
        this.databaseController = databaseController;
        this.configuration = configuration;
    }

    public void startCollector() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);
        completionService.submit(new CollectorTask(databaseController, configuration));
        try {
            completionService.take();
        } catch(InterruptedException e) {
            LOGGER.error("Error during execution of the collector: {}", e);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch(InterruptedException e) {
            LOGGER.error("Execution interrupted while awaiting termination: {}", e);
        }
    }
}
