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

import inspector.imondb.collector.model.config.Configuration;
import inspector.imondb.collector.view.ProgressReporter;

public class ExecutionController {

    private DatabaseController databaseController;
    private Configuration configuration;

    public ExecutionController(DatabaseController databaseController, Configuration configuration) {
        this.databaseController = databaseController;
        this.configuration = configuration;
    }

    public CollectorTask getCollectorTask(ProgressReporter progressReporter) {
        CollectorTask task = new CollectorTask(databaseController, configuration);
        task.setProgressReporter(progressReporter);

        return task;
    }
}
