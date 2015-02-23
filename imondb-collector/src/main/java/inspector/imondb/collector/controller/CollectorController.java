package inspector.imondb.collector.controller;

/*
 * #%L
 * iMonDB Collector
 * %%
 * Copyright (C) 2014 - 2015 InSPECtor
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import inspector.imondb.collector.controller.listeners.ConfigurationChangeListener;
import inspector.imondb.collector.controller.listeners.DatabaseConnectionListener;
import inspector.imondb.collector.model.config.Configuration;
import inspector.imondb.collector.view.cli.SystemOutProgressBar;
import inspector.imondb.collector.view.gui.CollectorFrame;
import inspector.imondb.collector.view.gui.about.AboutListener;
import inspector.imondb.collector.view.gui.exit.ExitAction;
import inspector.imondb.collector.view.gui.update.UpdateListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class CollectorController {

    private static final Logger LOGGER = LogManager.getLogger(CollectorController.class);

    private Configuration configuration;

    private CollectorFrame collector;

    private DatabaseController databaseController;
    private ExecutionController executionController;

    public CollectorController(String config) {
        // model
        configuration = new Configuration(new File(config));

        // controller
        databaseController = new DatabaseController();
        executionController = new ExecutionController(databaseController, configuration);
    }

    public void cleanUp() {
        configuration.store();
        databaseController.disconnect();
    }

    public void startGuiView() {
        // enable the text pane appender
        org.apache.logging.log4j.core.Logger rootLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        rootLogger.addAppender(rootLogger.getContext().getConfiguration().getAppender("textPane"));

        // view
        collector = new CollectorFrame(this, executionController, configuration);

        // listeners
        collector.addExitAction(new ExitAction());
        collector.addAboutDisplayer(new AboutListener("iMonDB Collector"));
        collector.addUpdateChecker(new UpdateListener("collector", "iMonDB Collector"));

        collector.addConfigurationChangeListener(new ConfigurationChangeListener(collector, configuration));
        collector.addDatabaseConnectionListener(new DatabaseConnectionListener(collector, databaseController));

        // start viewer
        SwingUtilities.invokeLater(() -> {
            collector.display();
            collector.initialize();
        });
    }

    public void startCliView() {
        // enable the console appender
        org.apache.logging.log4j.core.Logger rootLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        rootLogger.addAppender(rootLogger.getContext().getConfiguration().getAppender("console"));

        // start processing
        try {
            CollectorTask task = executionController.getCollectorTask(new SystemOutProgressBar());
            task.execute();
            task.get();
        } catch(InterruptedException | ExecutionException e) {
            LOGGER.error("Error while executing the collector: {}", e.getMessage(), e);
        }

        cleanUp();
    }
}
