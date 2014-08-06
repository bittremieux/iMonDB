package inspector.jmondb.collect;

import inspector.jmondb.convert.Thermo.ThermoRawFileExtractor;
import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBReader;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class Collector {

	protected static final Logger logger = LogManager.getLogger(Collector.class);

	public Collector() {

		EntityManagerFactory emf = null;

		Ini config = initializeConfig();

		try {
			// create database connection
			String host = config.get("sql", "host");
			host = host == null || host.equals("") ? null : host;
			String port = config.get("sql", "port");
			port = port == null || port.equals("") ? null : port;
			String database = config.get("sql", "database");
			if(database == null || database.equals("")) {
				logger.error("The MySQL database must be specified in the config file");
				throw new IllegalStateException("The MySQL database must be specified in the config file");
			}
			String user = config.get("sql", "user");
			if(user == null || user.equals("")) {
				logger.error("The MySQL user name must be specified in the config file");
				throw new IllegalStateException("The MySQL user name must be specified in the config file");
			}
			String password = config.get("sql", "password");
			password = password == null || password.equals("") ? null : password;
			emf = IMonDBManagerFactory.createMySQLFactory(host, port, database, user, password);

			IMonDBReader dbReader = new IMonDBReader(emf);
			IMonDBWriter dbWriter = new IMonDBWriter(emf);

			// read project folders
			Map<String, String> projects = config.get("projects");

			// read the cut-off date and regex used to match files
			String lastDate = config.get("general", "last_date");
			Date date;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			if(lastDate == null || lastDate.equals("")) {
				logger.info("No cut-off date specified, retrieving all eligible files");
				date = new Date(0);
			}
			else {
				date = sdf.parse(lastDate);
			}
			Timestamp newestTimeStamp = new Timestamp(date.getTime());

			String matchFile = config.get("general", "match_file");
			if(matchFile == null || matchFile.equals("")) {
				logger.error("The 'match_file' regex must be specified in the config file");
				throw new IllegalStateException("The 'match_file' regex must be specified in the config file");
			}

			String nameMask = config.get("general", "rename_run");
			if(nameMask == null || nameMask.equals("")) {
				nameMask = "%p_%dn_%fn";
				logger.info("No run rename mask specified, using default '{}'", nameMask);
			}

			// browse all folders and find new raw files
			for(Map.Entry<String, String> entry : projects.entrySet()) {
				File baseDir = new File(entry.getValue());
				if(!entry.getKey().equals("dummy") && baseDir.isDirectory()) {
					logger.info("Process project <{}>", entry.getKey());

					// retrieve all files that were created after the specified date, and matching the specified regex
					Collection<File> files = FileUtils.listFiles(baseDir,
							new AndFileFilter(new AgeFileFilter(date, false),
									new RegexFileFilter(matchFile, IOCase.INSENSITIVE)),
							DirectoryFileFilter.DIRECTORY);

					// process all found files
					for(File file : files) {
						logger.info("Process file <{}>", file.getAbsolutePath());

						String runName = nameMask.replace("%p", entry.getKey()).
								replace("%dn", FilenameUtils.getBaseName(file.getParent())).
								replace("%fn", FilenameUtils.getBaseName(file.getName()));

						// check if this run already exists in the database for the given project
						String runExistQuery = "SELECT COUNT(run) FROM Run run WHERE run.name = \"" + runName + "\" AND run.fromProject.label = \"" + entry.getKey() + "\"";
						boolean exists = dbReader.getFromCustomQuery(runExistQuery, Long.class).get(0).equals(1L);

						if(!exists) {
							ThermoRawFileExtractor extractor = new ThermoRawFileExtractor(file.getAbsolutePath());
							Run run = extractor.extractInstrumentData();

							// rename run based on the mask
							run.setName(runName);

							// write the run to the database
							dbWriter.writeRun(run, entry.getKey());

							// save the date of the newest file
							newestTimeStamp = newestTimeStamp.before(run.getSampleDate()) ? run.getSampleDate() : newestTimeStamp;
						}
						else {
							logger.info("Run <{}> already found in the database; skipping...", runName);
						}
					}
				} else {
					logger.error("Path <{}> is not a valid directory for project <{}>", entry.getValue(), entry.getKey());
					throw new IllegalArgumentException("Path <" + entry.getValue() + "> is not a valid directory");
				}
			}

			// save the date of the newest processed file to the config file
			config.put("general", "last_date", sdf.format(newestTimeStamp));

		} catch(ParseException e) {
			logger.error("Invalid cut-off date <{}> specified: ", config.get("general", "last_date"), e);
			throw new IllegalStateException("Invalid cut-off date specified: " + e);
		} finally {
			if(emf != null)
				emf.close();
			if(config != null)
				try {
					config.store();
				} catch(IOException e) {
					logger.error("Error while writing the updated config file: {}", e);
					throw new IllegalStateException("Error while writing the updated config file: " + e);
				}
		}
	}

	private Ini initializeConfig() {
		try {
			// check whether an explicit config file exists in the current directory
			File config = new File("config.ini");
			if(config.exists())
				return new Ini(config);
			else {
				// else load the standard config file
				logger.info("No user-specific config file found, loading the standard config file");

				Ini configStd = new Ini(Collector.class.getResourceAsStream("/config.ini"));
				configStd.setFile(new File("config.ini"));	// set the file to be able to store changes later on
				return configStd;
			}

		} catch(IOException e) {
			logger.error("Error while reading the config file: {}", e);
			throw new IllegalStateException("Error while reading the config file: " + e);
		}
	}
}
