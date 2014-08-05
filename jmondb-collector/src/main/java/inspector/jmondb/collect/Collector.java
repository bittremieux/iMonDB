package inspector.jmondb.collect;

import inspector.jmondb.convert.Thermo.ThermoRawFileExtractor;
import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBWriter;
import inspector.jmondb.model.Run;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;

import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class Collector {

	protected static final Logger logger = LogManager.getLogger(Collector.class);

	public Collector() {

		Ini config = initializeConfig();

		// create database connection
		EntityManagerFactory emf = IMonDBManagerFactory.createMySQLFactory(config.get("sql", "host"),
				config.get("sql", "port"), config.get("sql", "database"),
				config.get("sql", "user"), config.get("sql", "password"));

		try {
			IMonDBWriter dbWriter = new IMonDBWriter(emf);

			// read project folders
			Map<String, String> projects = config.get("projects");

			// read the cut-off date and regex used to match files
			String lastDate = config.get("general", "last_date");
			Date date;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			if(lastDate == null) {
				logger.info("No cut-off date specified, retrieving all eligible files");
				date = new Date(0);
			}
			else {
				date = sdf.parse(lastDate);
			}
			Timestamp newestTimeStamp = new Timestamp(date.getTime());
			String match_file = config.get("general", "match_file");

			// browse all folders and find new raw files
			for(Map.Entry<String, String> entry : projects.entrySet()) {
				File baseDir = new File(entry.getValue());
				if(baseDir.isDirectory()) {
					// retrieve all files that were created after the specified date, and matching the specified regex
					Collection<File> files = FileUtils.listFiles(baseDir,
							new AndFileFilter(new AgeFileFilter(date, false),
									new RegexFileFilter(match_file, IOCase.INSENSITIVE)),
							DirectoryFileFilter.DIRECTORY);

					// process all found files
					for(File file : files) {
						ThermoRawFileExtractor extractor = new ThermoRawFileExtractor(file.getAbsolutePath());
						Run run = extractor.extractInstrumentData();

						// rename run based on the mask
						String nameMask = config.get("general", "rename_run");
						if(nameMask == null)
							nameMask = "%p_%dn_%fn";
						String runName = nameMask.replace("%p", entry.getKey()).
								replace("%dn", FilenameUtils.getBaseName(file.getParent())).
								replace("%fn", FilenameUtils.getBaseName(file.getName()));
						run.setName(runName);

						// write the run to the database
						// TODO: verify if the run was already in the database?
						dbWriter.writeRun(run, entry.getKey());

						// save the date of the newest file
						newestTimeStamp = newestTimeStamp.before(run.getSampleDate()) ? run.getSampleDate() : newestTimeStamp;
					}
				} else {
					logger.error("Path <{}> is not a valid directory for project <{}>", entry.getValue(), entry.getKey());
					throw new IllegalArgumentException("Path <" + entry.getValue() + "> is not a valid directory");
				}
			}

			// save the date of the newest processed file to the config file
			config.put("general", "last_date", sdf.format(newestTimeStamp));
			config.store();

		} catch(ParseException e) {
			logger.error("Invalid cut-off date <{}> specified: ", config.get("general", "last_date"), e);
			throw new IllegalStateException("Invalid cut-off date specified: " + e);
		} catch(IOException e) {
			logger.error("Error while writing the updated config file: {}", e);
			throw new IllegalStateException("Error while writing the updated config file: " + e);
		} finally {
			emf.close();
		}
	}

	private Ini initializeConfig() {
		try {
			// check whether the config file was specified as argument
			String systemConfig = System.getProperty("config.ini");
			if(systemConfig != null) {
				File configFile = new File(systemConfig);
				if(!configFile.exists()) {
					logger.error("The config file <{}> does not exist", systemConfig);
					throw new IllegalArgumentException("The config file to read does not exist: " + systemConfig);
				}
				else
					return new Ini(configFile);
			}

			// else load the config file
			return new Ini(Collector.class.getResourceAsStream("/config.ini"));

		} catch(IOException e) {
			logger.error("Error while reading the config file: {}", e);
			throw new IllegalStateException("Error while reading the config file: " + e);
		}
	}
}
