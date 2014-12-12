package inspector.jmondb.collect;

/*
 * #%L
 * jMonDB Collector
 * %%
 * Copyright (C) 2014 InSPECtor
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
import inspector.jmondb.config.ConfigFile;
import inspector.jmondb.config.MetadataMapper;
import inspector.jmondb.convert.thermo.ThermoRawFileExtractor;
import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBReader;
import inspector.jmondb.io.IMonDBWriter;
import inspector.jmondb.model.CV;
import inspector.jmondb.model.Instrument;
import inspector.jmondb.model.InstrumentModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManagerFactory;
import java.io.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * A collector that will collect the instrument data for all raw files and store it in the iMonDB.
 *
 * Specific settings are read from the associated config file.
 */
public class Collector {

	protected static final Logger logger = LogManager.getLogger(Collector.class);

	/** a YAML configuration file */
	private ConfigFile config;

	//TODO: unify cv handling??
	private static CV cvMS = new CV("MS", "PSI MS controlled vocabulary", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0");

	/**
	 * Creates a collector that will collect the instrument data for all raw files and store it in the iMonDB.
	 *
	 * Specific settings are read from the associated config file.
	 */
	public Collector() {
		config = initializeConfigReader();
	}

	/**
	 * Collects the instrument data for all the raw files based on the settings in the config file.
	 */
	public void collect() {
		logger.info("Executing the collector");

		EntityManagerFactory emf = null;

		try {
			// create database connection
			emf = getEntityManagerFactory();
			IMonDBReader dbReader = new IMonDBReader(emf);
			IMonDBWriter dbWriter = new IMonDBWriter(emf);

			// raw file extractor
			ThermoRawFileExtractor extractor = new ThermoRawFileExtractor();
			MetadataMapper metadataMapper = config.getMetadataMapper();

			// read the general information from the config file
			Timestamp newestTimestamp = config.getLastDate();

			boolean forceUnique = config.getForceUniqueFileNames();
			String matchFile = config.getMatchFile();

			// thread pool
			int nrOfThreads = config.getNumberOfThreads();
			ExecutorService threadPool = Executors.newFixedThreadPool(nrOfThreads);
			CompletionService<Timestamp> pool = new ExecutorCompletionService<>(threadPool);
			int threadsSubmitted = 0;

			// make sure all required instruments are present in the database
			addNewInstruments(dbReader, dbWriter, config.getInstruments());

			// browse the start directory and underlying directories to find new raw files
			File startDir = config.getStartDirectory();
			try {
				logger.debug("Process directory <{}>", startDir.getCanonicalPath());

				// retrieve all files that were created after the specified date, and matching the specified regex
				Collection<File> files = FileUtils.listFiles(startDir,
						new AndFileFilter(new AgeFileFilter(new Date(newestTimestamp.getTime()), false),
							new RegexFileFilter(matchFile, IOCase.INSENSITIVE)),
						DirectoryFileFilter.DIRECTORY);

				// process all found files
				for(File file : files) {
					// retrieve the instrument name from the file name and path based on the configuration
					String instrumentName = config.getInstrumentNameForFile(file);

					if(instrumentName != null) {
						logger.trace("Add file <{}> for instrument <{}> to the thread pool", file.getCanonicalPath(), instrumentName);
						pool.submit(new FileProcessor(dbReader, dbWriter, extractor, metadataMapper, file, instrumentName, forceUnique));
						threadsSubmitted++;
					}
				}
			} catch(IOException e) {
				logger.fatal("IO error: {}", e.getMessage());
				throw new IllegalArgumentException("IO error: " + e.getMessage());
			}

			// process all the submitted threads and retrieve the sample dates
			for(int i = 0; i < threadsSubmitted; i++) {
				try {
					logger.info("Processing file {} out of a total of {} queued files", (i+1), threadsSubmitted);
					Timestamp runTimestamp = pool.take().get();
					newestTimestamp = runTimestamp != null && newestTimestamp.before(runTimestamp) ? runTimestamp : newestTimestamp;
				} catch(Exception e) {	// catch all possible exceptions that were thrown during the processing of this individual file to correctly continue processing the other files
					logger.error("Error while executing a thread: {}", e.getMessage(), e);
				}
			}

			// shut down child threads
			threadPool.shutdown();
			// wait until all child threads have finished
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			// save the date of the newest processed file to the config file
			config.setLastDate(newestTimestamp);

		} catch(InterruptedException e) {
			logger.error("Thread execution was interrupted: {}", e.getMessage());
		} finally {
			// close the database connection
			if(emf != null)
				emf.close();
			// save the (updated) config file to the user directory
			config.store();
		}
	}

	/**
	 * Loads a {@link ConfigFile}.
	 *
	 * If a user-specific config file exists, this one is used. Otherwise the (incomplete) standard config file is used.
	 *
	 * @return the loaded {@code ConfigFile}
	 */
	private ConfigFile initializeConfigReader() {
		try {
			InputStream inputStream;
			// check whether an explicit config file exists in the current directory
			File config = new File("config.yaml");
			if(config.exists()) {
				logger.debug("Load user-specific config file");
				inputStream = new FileInputStream(config);
			}
			else {
				// else load the standard config file
				logger.debug("No user-specific config file found, loading the standard config file");
				inputStream = Collector.class.getResourceAsStream("/config.yaml");
			}

			ConfigFile configFile = new ConfigFile(inputStream);
			inputStream.close();

			return configFile;

		} catch(IOException e) {
			logger.error("Error while loading the config file: {}", e.getMessage());
			throw new IllegalStateException("Error while loading the config file: " + e.getMessage());
		}
	}

	/**
	 * Creates an {@link EntityManagerFactory} to connect to the database based on the information in the config file.
	 *
	 * @return an {@code EntityManagerFactory} to connect to the database
	 */
	private EntityManagerFactory getEntityManagerFactory() {
		return IMonDBManagerFactory.createMySQLFactory(config.getDatabaseHost(), config.getDatabasePort(),
				config.getDatabaseName(), config.getDatabaseUser(), config.getDatabasePassword());
	}

	/**
	 * Makes sure that all {@link Instrument}s that are defined in the config file are present in the database.
	 *
	 * If a specific {@code Instrument} is not in the database yet, it will be added.
	 *
	 * @param reader  the {@link IMonDBReader} used to check whether an {@code Instrument} is already in the database
	 * @param writer  the {@link IMonDBWriter} used to write a new {@code Instrument} to the database
	 * @param instruments  a {@code List} of {@code Map}s where each {@code Map} contains information about a single {@code Instrument}
	 */
	private void addNewInstruments(IMonDBReader reader, IMonDBWriter writer, List<Map<String, String>> instruments) {
		for(Map<String, String> instrument : instruments) {
			// check if the instrument is already in the database
			Map<String, String> parameters = ImmutableMap.of("name", instrument.get("name"));
			String query = "SELECT COUNT(inst) FROM Instrument inst WHERE inst.name = :name";
			boolean exists = reader.getFromCustomQuery(query, Long.class, parameters).get(0).equals(1L);

			// else, add it to the database
			if(!exists)
				writer.writeInstrument(new Instrument(instrument.get("name"), InstrumentModel.fromString(instrument.get("type")), cvMS));
			else
				logger.trace("Instrument <{}> found in the database", instrument.get("name"));
		}
	}
}
