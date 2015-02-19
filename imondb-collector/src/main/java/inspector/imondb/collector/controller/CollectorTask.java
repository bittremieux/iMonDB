package inspector.imondb.collector.controller;

import com.google.common.collect.ImmutableMap;
import inspector.imondb.collector.model.InstrumentMap;
import inspector.imondb.collector.model.MetadataMap;
import inspector.imondb.collector.model.RegexMapper;
import inspector.imondb.collector.model.config.Configuration;
import inspector.imondb.collector.model.config.DatabaseConfiguration;
import inspector.imondb.collector.model.config.GeneralConfiguration;
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

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CollectorTask implements Callable<Void> {

    private static final Logger LOGGER = LogManager.getLogger(CollectorTask.class);

    private DatabaseController databaseController;
    private Configuration configuration;

    public CollectorTask(DatabaseController databaseController, Configuration configuration) {
        this.databaseController = databaseController;
        this.configuration = configuration;
    }

    @Override
    public Void call() throws Exception {
        LOGGER.info("Executing the collector");

        try {
            // create database connection
            DatabaseConfiguration dbConfig = configuration.getDatabaseConfiguration();
            databaseController.connectTo(dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase(),
                    dbConfig.getUserName(), dbConfig.getPassword());
            IMonDBReader dbReader = databaseController.getReader();
            IMonDBWriter dbWriter = databaseController.getWriter();

            // raw file extractor
            ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();

            // read the general information from the config file
            GeneralConfiguration genConfig = configuration.getGeneralConfiguration();
            String fileNameRegex = genConfig.getFileNameRegex();
            Timestamp newestTimestamp = genConfig.getStartDate() != null ? genConfig.getStartDate() : new Timestamp(new Date(0).getTime());
            boolean forceUnique = genConfig.getUniqueFileNames();

            // thread pool
            int nrOfThreads = genConfig.getNumberOfThreads();
            ExecutorService threadPool = Executors.newFixedThreadPool(nrOfThreads);
            CompletionService<Timestamp> pool = new ExecutorCompletionService<>(threadPool);
            int threadsSubmitted = 0;

            // instrument and metadata mappings
            RegexMapper<InstrumentMap> instrumentMapper = new RegexMapper<>(configuration.getInstrumentConfiguration().getInstruments());
            RegexMapper<MetadataMap> metadataMapper = new RegexMapper<>(configuration.getMetadataConfiguration().getMetadata());

            // make sure all required instruments are present in the database
            createNewInstruments(dbReader, dbWriter, configuration.getInstrumentConfiguration().getInstruments());

            // browse the start directory and underlying directories to find new raw files
            File startDir = new File(genConfig.getDirectory());
            try {
                LOGGER.debug("Process directory <{}>", startDir.getCanonicalPath());

                // retrieve all files that were created after the specified date, and matching the specified regex
                Collection<File> files = FileUtils.listFiles(startDir, new AndFileFilter(
                                new AgeFileFilter(new Date(newestTimestamp.getTime()), false),
                                new RegexFileFilter(fileNameRegex, IOCase.INSENSITIVE)),
                        DirectoryFileFilter.DIRECTORY);

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

            // process all the submitted threads and retrieve the sample dates
            for(int i = 0; i < threadsSubmitted; i++) {
                try {
                    LOGGER.info("Processing file {} out of a total of {} queued files", (i + 1), threadsSubmitted);
                    Timestamp runTimestamp = pool.take().get();
                    newestTimestamp = runTimestamp != null && newestTimestamp.before(runTimestamp) ? runTimestamp : newestTimestamp;
                } catch(Exception e) {
                    // catch all possible exceptions that were thrown during the processing of this individual file to correctly continue processing the other files
                    LOGGER.error("Error while executing a thread: {}", e.getMessage(), e);
                }
            }

            // shut down child threads
            threadPool.shutdown();
            // wait until all child threads have finished
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            // save the date of the newest processed file to the config file
            genConfig.setStartDate(newestTimestamp);

        } catch(InterruptedException e) {
            LOGGER.error("Thread execution was interrupted: {}", e);
        } finally {
            // save the (updated) config file to the user directory
            configuration.store();
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
}
