package inspector.imondb.collect;

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
import inspector.imondb.config.MetadataMapper;
import inspector.imondb.convert.thermo.ThermoRawFileExtractor;
import inspector.imondb.io.IMonDBReader;
import inspector.imondb.io.IMonDBWriter;
import inspector.imondb.model.Run;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Processes a raw file by extracting the instrument data and storing the resulting {@link Run} in the database.
 *
 * Can be executed in its own thread.
 */
public class FileProcessor implements Callable<Timestamp> {

	protected static final Logger logger = LogManager.getLogger(FileProcessor.class);

	private IMonDBReader dbReader;
	private IMonDBWriter dbWriter;
	private ThermoRawFileExtractor extractor;
	private MetadataMapper metadataMapper;
	private File file;
	private String instrumentName;
	private boolean forceUnique;

	/**
	 * Processes a file by extracting the instrument data from it and storing the resulting run in the database.
	 *
	 * @param dbReader  The {@link IMonDBReader} used to verify the current file isn't present in the database yet
	 * @param dbWriter  The {@link IMonDBWriter} used to write the new {@link Run} to the database
	 * @param file  The raw file that will be processed
	 */

	/**
	 * Processes a file by extracting the instrument data from it and storing the resulting run in the database.
	 *
	 * @param dbReader  the {@link IMonDBReader} used to verify the current file is not present in the database yet
	 * @param dbWriter  the {@link IMonDBWriter} used to write the new {@link Run} to the database
	 * @param extractor  the {@link ThermoRawFileExtractor} used to extract the instrument data from the raw file
	 * @param metadataMapper  the {@link MetadataMapper} used to obtain metadata based on the config file
	 * @param file  the raw file that will be processed
	 * @param instrumentName  the (unique) name of the instrument on which the run was performed
	 * @param forceUnique  flag which indicates whether file names have to be made unique explicitly
	 */
	public FileProcessor(IMonDBReader dbReader, IMonDBWriter dbWriter, ThermoRawFileExtractor extractor, MetadataMapper metadataMapper, File file, String instrumentName, boolean forceUnique) {
		this.dbReader = dbReader;
		this.dbWriter = dbWriter;
		this.extractor = extractor;
		this.metadataMapper = metadataMapper;
		this.file = file;
		this.instrumentName = instrumentName;
		this.forceUnique = forceUnique;
	}

	@Override
	public Timestamp call() {
		logger.info("Process file <{}>", file.getAbsolutePath());

		String runName = FilenameUtils.getBaseName(file.getName());
		if(forceUnique) {
			// append the MD5 checksum to enforce unique file names
			try {
				FileInputStream fis = new FileInputStream(file);

				String md5 = DigestUtils.md5Hex(fis);
				runName += "_" + md5;

				fis.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		// check if this run already exists in the database for the given instrument
		Map<String, String> parameters = ImmutableMap.of("runName", runName, "instName", instrumentName);
		String runExistQuery = "SELECT COUNT(run) FROM Run run WHERE run.name = :runName AND run.instrument.name = :instName";
		boolean exists = dbReader.getFromCustomQuery(runExistQuery, Long.class, parameters).get(0).equals(1L);

		if(!exists) {
			Run run = extractor.extractInstrumentData(file.getAbsolutePath(), runName, instrumentName);

			// extract metadata
			if(metadataMapper != null)
				metadataMapper.applyMetadata(run, file);

			// write the run to the database
            synchronized(FileProcessor.class) {
                dbWriter.writeRun(run);
            }

			// return the run's sample date
			return run.getSampleDate();
		}
		else {
			logger.trace("Run <{}> already found in the database; skipping...", runName);
			return null;
		}
	}
}
