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

import com.pagosoft.plaf.PlafOptions;
import inspector.imondb.collector.controller.listeners.ConfigurationChangeListener;
import inspector.imondb.collector.controller.listeners.DatabaseConnectionListener;
import inspector.imondb.collector.model.config.Configuration;
import inspector.imondb.collector.view.CollectorFrame;
import inspector.imondb.collector.view.about.AboutListener;
import inspector.imondb.collector.view.exit.ExitAction;
import inspector.imondb.collector.view.update.UpdateListener;

import javax.swing.*;
import java.io.File;

public class CollectorController {

    private Configuration configuration;

    private CollectorFrame collector;

    private DatabaseController databaseController;

    public CollectorController() {

        // app information
        String name = "iMonDB Collector";
        String id = "collector";

        // model
        configuration = new Configuration(new File("config.yaml"));

        // controller
        databaseController = new DatabaseController();

        // view
        collector = new CollectorFrame(this, configuration);

        // listeners
        collector.addExitAction(new ExitAction());
        collector.addAboutDisplayer(new AboutListener(name));
        collector.addUpdateChecker(new UpdateListener(id, name));

        collector.addConfigurationChangeListener(new ConfigurationChangeListener(collector, configuration));
        collector.addDatabaseConnectionListener(new DatabaseConnectionListener(collector, databaseController));
    }

    public static void main(String[] args) {
        setLookAndFeel();

        // start viewer
        SwingUtilities.invokeLater(() -> {
            CollectorController controller = new CollectorController();
            controller.collector.display();
            controller.collector.initialize();
        });
    }

    private static void setLookAndFeel() {
        PlafOptions.setAsLookAndFeel();
        PlafOptions.updateAllUIs();
    }

    public void cleanUp() {
        configuration.store();
        databaseController.disconnect();
    }
}
