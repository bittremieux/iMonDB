package inspector.jmondb.collect;

import inspector.jmondb.convert.Thermo.ThermoRawFileExtractor;
import inspector.jmondb.io.IMonDBReader;
import inspector.jmondb.io.IMonDBWriter;
import inspector.jmondb.model.Run;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Timestamp;
import java.util.concurrent.Callable;

public class FileProcessor implements Callable<Timestamp> {

	protected static final Logger logger = LogManager.getLogger(FileProcessor.class);

	private IMonDBReader dbReader;
	private IMonDBWriter dbWriter;
	private String renameMask;
	private String projectLabel;
	private File file;

	/**
	 * Processes a file by extracting the instrument data from it and storing the resulting run in the database.
	 *
	 * @param dbReader  The {@link IMonDBReader} used to verify the current file isn't present in the database yet
	 * @param dbWriter  The {@link IMonDBWriter} used to write the new {@link Run} to the database
	 * @param renameMask  The mask used to rename the run's name
	 * @param projectLabel  The label of the project to which the run belongs
	 * @param file  The raw file that will be processed
	 */
	public FileProcessor(IMonDBReader dbReader, IMonDBWriter dbWriter, String renameMask, String projectLabel, File file) {
		this.dbReader = dbReader;
		this.dbWriter = dbWriter;
		this.renameMask = renameMask;
		this.projectLabel = projectLabel;
		this.file = file;
	}

	@Override
	public Timestamp call() {
		logger.info("Process file <{}>", file.getAbsolutePath());

		String runName = renameMask.replace("%p", projectLabel).
				replace("%dn", FilenameUtils.getBaseName(file.getParent())).
				replace("%fn", FilenameUtils.getBaseName(file.getName()));

		// check if this run already exists in the database for the given project
		String runExistQuery = "SELECT COUNT(run) FROM Run run WHERE run.name = \"" + runName + "\" AND run.fromProject.label = \"" + projectLabel + "\"";
		boolean exists = dbReader.getFromCustomQuery(runExistQuery, Long.class).get(0).equals(1L);

		if(!exists) {
			ThermoRawFileExtractor extractor = new ThermoRawFileExtractor(file.getAbsolutePath());
			Run run = extractor.extractInstrumentData();

			// rename run based on the mask
			run.setName(runName);

			// write the run to the database
			dbWriter.writeRun(run, projectLabel);

			// return the run's execution time
			return run.getSampleDate();
		}
		else {
			logger.info("Run <{}> already found in the database; skipping...", runName);
			return null;
		}
	}
}
