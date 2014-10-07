package inspector.jmondb.collect;

import com.google.common.collect.ImmutableMap;
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

public class FileProcessor implements Callable<Timestamp> {

	protected static final Logger logger = LogManager.getLogger(FileProcessor.class);

	private IMonDBReader dbReader;
	private IMonDBWriter dbWriter;
	private ThermoRawFileExtractor extractor;
	private String renameMask;
	private File file;
	private String instrumentName;

	/**
	 * Processes a file by extracting the instrument data from it and storing the resulting run in the database.
	 *
	 * @param dbReader  The {@link IMonDBReader} used to verify the current file isn't present in the database yet
	 * @param dbWriter  The {@link IMonDBWriter} used to write the new {@link Run} to the database
	 * @param renameMask  The mask used to rename the run's name
	 * @param file  The raw file that will be processed
	 */
	public FileProcessor(IMonDBReader dbReader, IMonDBWriter dbWriter, ThermoRawFileExtractor extractor, String renameMask, File file, String instrumentName) {
		this.dbReader = dbReader;
		this.dbWriter = dbWriter;
		this.extractor = extractor;
		this.renameMask = renameMask;
		this.file = file;
		this.instrumentName = instrumentName;
	}

	@Override
	public Timestamp call() {
		logger.info("Process file <{}>", file.getAbsolutePath());

		String runName = renameMask.replace("%dn", FilenameUtils.getBaseName(file.getParent())).
				replace("%fn", FilenameUtils.getBaseName(file.getName()));

		// check if this run already exists in the database for the given project
		Map<String, String> parameters = ImmutableMap.of("runName", runName);
		String runExistQuery = "SELECT COUNT(run) FROM Run run WHERE run.name = :runName";
		boolean exists = dbReader.getFromCustomQuery(runExistQuery, Long.class, parameters).get(0).equals(1L);

		if(!exists) {
			Run run = extractor.extractInstrumentData(file.getAbsolutePath(), runName, instrumentName);

			// write the run to the database
			dbWriter.writeRun(run);

			// return the run's sample date
			return run.getSampleDate();
		}
		else {
			logger.info("Run <{}> already found in the database; skipping...", runName);
			return null;
		}
	}
}
