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

import com.google.common.collect.ImmutableMap;
import inspector.imondb.collector.model.InstrumentMap;
import inspector.imondb.collector.model.MetadataMap;
import inspector.imondb.collector.model.RegexMapper;
import inspector.imondb.collector.model.config.Configuration;
import inspector.imondb.collector.model.config.DatabaseConfiguration;
import inspector.imondb.collector.model.config.GeneralConfiguration;
import inspector.imondb.collector.view.ProgressReporter;
import inspector.imondb.convert.thermo.ThermoRawFileExtractor;
import inspector.imondb.io.IMonDBReader;
import inspector.imondb.io.IMonDBWriter;
import inspector.imondb.model.CV;
import inspector.imondb.model.Instrument;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CollectorTask extends SwingWorker<Void, Integer> {

    private static final Logger LOGGER = LogManager.getLogger(CollectorTask.class);

    private ProgressReporter progressReporter;

    private DatabaseController databaseController;
    private Configuration configuration;

    private ExecutorService threadPool;

    public CollectorTask(DatabaseController databaseController, Configuration configuration) {
        this.databaseController = databaseController;
        this.configuration = configuration;
    }

    public void setProgressReporter(ProgressReporter progressReporter) {
        this.progressReporter = progressReporter;
    }

    @Override
    protected Void doInBackground() throws Exception {
        LOGGER.info("Executing the collector");

        try {
            // thread pool
            GeneralConfiguration genConfig = configuration.getGeneralConfiguration();
            int nrOfThreads = genConfig.getNumberOfThreads();
            threadPool = Executors.newFixedThreadPool(nrOfThreads);
            CompletionService<Timestamp> pool = new ExecutorCompletionService<>(threadPool);

            // create database connection
            DatabaseConfiguration dbConfig = configuration.getDatabaseConfiguration();
            databaseController.connectTo(dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase(),
                    dbConfig.getUserName(), dbConfig.getPassword());
            IMonDBReader dbReader = databaseController.getReader();
            IMonDBWriter dbWriter = databaseController.getWriter();

            // read the general information from the config file
            Timestamp newestTimestamp = genConfig.getStartDate() != null ? genConfig.getStartDate() : new Timestamp(new Date(0).getTime());

            // make sure all required instruments are present in the database
            createNewInstruments(dbReader, dbWriter, configuration.getInstrumentConfiguration().getInstruments());

            // browse the start directory and underlying directories to find new raw files
            File startDir = new File(genConfig.getDirectory());
            int threadsSubmitted = submitTasks(startDir, pool, dbReader, dbWriter, newestTimestamp);

            // process all the submitted threads and retrieve the sample dates
            for(int i = 0; i < threadsSubmitted; i++) {
                LOGGER.info("Processing file {} out of a total of {} queued files", i + 1, threadsSubmitted);
                Timestamp runTimestamp = retrieveTask(pool);
                newestTimestamp = runTimestamp != null && newestTimestamp.before(runTimestamp) ? runTimestamp : newestTimestamp;
                // update progress
                publish((i+1) * 100 / threadsSubmitted);
            }

            // shut down child threads
            threadPool.shutdown();
            // wait until all child threads have finished
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            // save the date of the newest processed file to the config file
            genConfig.setStartDate(newestTimestamp);
            // save the (updated) config file to the user directory
            configuration.store();

            LOGGER.info("Processing finished successfully");

        } catch(InterruptedException e) {
            LOGGER.error("Thread execution was interrupted: {}", e.getMessage(), e);
        } finally {
            // close the database connection
            databaseController.disconnect();
        }

        return null;
    }

    private void createNewInstruments(IMonDBReader reader, IMonDBWriter writer, Collection<InstrumentMap> instrumentMaps) {
        CV cv = new CV("MS", "PSI MS controlled vocabulary", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0");

        for(InstrumentMap instrumentMap : instrumentMaps) {
            // check if the instrument already exists in the database
            Map<String, String> parameters = ImmutableMap.of("name", instrumentMap.getKey());
            String query = "SELECT COUNT(inst) FROM Instrument inst WHERE inst.name = :name";
            boolean exists = reader.getFromCustomQuery(query, Long.class, parameters).get(0).equals(1L);

            // else, add it to the database
            if(!exists) {
                LOGGER.trace("Add instrument <{}> to the database", instrumentMap.getKey());
                writer.writeInstrument(new Instrument(instrumentMap.getKey(), instrumentMap.getValue(), cv));
            } else {
                LOGGER.trace("Instrument <{}> found in the database", instrumentMap.getKey());
            }
        }
    }

    private int submitTasks(File startDir, CompletionService<Timestamp> pool,
                            IMonDBReader dbReader, IMonDBWriter dbWriter, Timestamp newestTimestamp) {
        int threadsSubmitted = 0;
        try {
            LOGGER.debug("Process directory <{}>", startDir.getCanonicalPath());

            String fileNameRegex = configuration.getGeneralConfiguration().getFileNameRegex();
            boolean forceUnique = configuration.getGeneralConfiguration().getUniqueFileNames();

            // retrieve all files that were created after the specified date, and matching the specified regex
            Collection<File> files = FileUtils.listFiles(startDir, new AndFileFilter(
                            new AgeFileFilter(new Date(newestTimestamp.getTime()), false),
                            new RegexFileFilter(fileNameRegex, IOCase.INSENSITIVE)),
                    DirectoryFileFilter.DIRECTORY);

            // raw file extractor
            ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();

            // instrument and metadata mappings
            RegexMapper<InstrumentMap> instrumentMapper = new RegexMapper<>(configuration.getInstrumentConfiguration().getInstruments());
            RegexMapper<MetadataMap> metadataMapper = new RegexMapper<>(configuration.getMetadataConfiguration().getMetadata());

            // process all found files
            for(File file : files) {
                // retrieve the instrument name from the file name and path based on the configuration
                List<InstrumentMap> applicableInstruments = instrumentMapper.getApplicableMaps(file);
                if(applicableInstruments.isEmpty()) {
                    LOGGER.warn("No instrument applicable for file <{}>; skipping...", file.getCanonicalPath());
                } else if(applicableInstruments.size() > 1) {
                    LOGGER.error("Multiple instruments applicable for file <{}>; skipping...", file.getCanonicalPath());
                } else {
                    InstrumentMap instrumentMap = applicableInstruments.get(0);
                    LOGGER.trace("Add file <{}> for instrument <{}> to the thread pool", file.getCanonicalPath(), instrumentMap.getKey());
                    pool.submit(new FileProcessor(dbReader, dbWriter, extractor, file, instrumentMap, forceUnique, metadataMapper));
                    threadsSubmitted++;
                }
            }
        } catch(IOException e) {
            LOGGER.fatal("IO error: {}", e);
            throw new IllegalArgumentException("IO error: " + e.getMessage());
        }
        return threadsSubmitted;
    }

    private Timestamp retrieveTask(CompletionService<Timestamp> pool) throws InterruptedException {
        try {
            return pool.take().get();
        } catch(ExecutionException e) {
            // all exceptions will be wrapped in an ExecutionException
            // catch the exceptions that were thrown during the processing of this individual file to correctly continue processing the other files
            LOGGER.error("Error while executing a thread: {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void done() {
        if(progressReporter != null) {
            progressReporter.done();
        }
    }

    @Override
    protected void process(List<Integer> chunks) {
        if(progressReporter != null) {
            chunks.forEach(progressReporter::setProgress);
        }
    }

    public void cancelExecution() {
        cancel(true);
        threadPool.shutdownNow();
    }
}
