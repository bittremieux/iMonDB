package inspector.jmondb.collect;

import com.google.common.collect.ImmutableMap;
import inspector.jmondb.config.MetadataMapper;
import inspector.jmondb.convert.Thermo.ThermoRawFileExtractor;
import inspector.jmondb.io.IMonDBReader;
import inspector.jmondb.io.IMonDBWriter;
import inspector.jmondb.model.Run;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Processes a raw file by extracting the instrument data and storing the resulting {@link Run} in the database.
 *
 * Can be executed in its own thread.
 */
public class FileProcessor implements Callable<Timestamp> {

	protected static final Logger logger = LogManager.getLogger(FileProcessor.class);

	/** static lock to make sure only one thread is writing to the database simultaneously */
	private static final Lock DATABASE_LOCK = new ReentrantLock();

	private IMonDBReader dbReader;
	private IMonDBWriter dbWriter;
	private ThermoRawFileExtractor extractor;
	private MetadataMapper metadataMapper;
	private File file;
	private String instrumentName;

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
	 */
	public FileProcessor(IMonDBReader dbReader, IMonDBWriter dbWriter, ThermoRawFileExtractor extractor, MetadataMapper metadataMapper, File file, String instrumentName) {
		this.dbReader = dbReader;
		this.dbWriter = dbWriter;
		this.extractor = extractor;
		this.metadataMapper = metadataMapper;
		this.file = file;
		this.instrumentName = instrumentName;
	}

	@Override
	public Timestamp call() {
		logger.info("Process file <{}>", file.getAbsolutePath());

		String runName = FilenameUtils.getBaseName(file.getName());

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
			try {
				DATABASE_LOCK.lock();
				dbWriter.writeRun(run);
			}
			finally {
				DATABASE_LOCK.unlock();
			}

			// return the run's sample date
			return run.getSampleDate();
		}
		else {
			logger.info("Run <{}> already found in the database; skipping...", runName);
			return null;
		}
	}
}
