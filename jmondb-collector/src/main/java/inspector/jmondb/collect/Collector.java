package inspector.jmondb.collect;

import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBReader;
import inspector.jmondb.io.IMonDBWriter;
import org.apache.commons.io.FileUtils;
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
import java.util.concurrent.*;

public class Collector {

	protected static final Logger logger = LogManager.getLogger(Collector.class);

	/** Date formatter to convert to and from date strings */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	public Collector() {

		EntityManagerFactory emf = null;

		Ini config = initializeConfig();

		try {
			// create database connection
			emf = getEntityManagerFactory(config);
			IMonDBReader dbReader = new IMonDBReader(emf);
			IMonDBWriter dbWriter = new IMonDBWriter(emf);

			// read project folders
			Map<String, String> projects = config.get("projects");

			// read the cut-off date and regex used to match files
			Date newestDate = getLastDate(config);
			Timestamp newestTimestamp = new Timestamp(newestDate.getTime());

			String matchFile = getMatchFile(config);
			String renameMask = getRenameMask(config);

			// thread pool
			int nrOfThreads = getNumberOfThreads(config);
			ExecutorService threadPool = Executors.newFixedThreadPool(nrOfThreads);
			CompletionService<Timestamp> pool = new ExecutorCompletionService<>(threadPool);
			int threadsSubmitted = 0;

			// browse all folders and find new raw files
			for(Map.Entry<String, String> entry : projects.entrySet()) {
				String projectLabel = entry.getKey();
				String projectDir = entry.getValue();

				File baseDir = new File(projectDir);
				if(!projectLabel.equals("dummy") && baseDir.isDirectory()) {
					logger.info("Process project <{}>", projectLabel);

					// retrieve all files that were created after the specified date, and matching the specified regex
					Collection<File> files = FileUtils.listFiles(baseDir,
							new AndFileFilter(new AgeFileFilter(newestDate, false),
									new RegexFileFilter(matchFile, IOCase.INSENSITIVE)),
							DirectoryFileFilter.DIRECTORY);

					// process all found files
					for(File file : files) {
						pool.submit(new FileProcessor(dbReader, dbWriter, renameMask, projectLabel, file));
						threadsSubmitted++;
					}

				} else if(!projectLabel.equals("dummy")) {
					logger.error("Path <{}> is not a valid directory for project <{}>", projectDir, projectLabel);
					throw new IllegalArgumentException("Path <" + projectDir + "> is not a valid directory");
				}
			}

			// retrieve the sample dates from all the submitted threads
			for(int i = 0; i < threadsSubmitted; i++) {
				Timestamp runTimestamp = pool.take().get();
				newestTimestamp = newestTimestamp.before(runTimestamp) ? runTimestamp : newestTimestamp;
			}

			// shut down threads
			threadPool.shutdown();

			// save the date of the newest processed file to the config file
			config.put("general", "last_date", DATE_FORMAT.format(newestTimestamp));

		} catch(InterruptedException | ExecutionException e) {
			logger.error("Error while executing a thread: {}", e);
			throw new IllegalStateException("Error while executing a thread: " + e);
		} finally {
			// close the database connection
			if(emf != null)
				emf.close();
			// make sure the config file is copied to the user directory
			if(config != null)
				try {
					config.store();
				} catch(IOException e) {
					logger.error("Error while writing the updated config file: {}", e);
				}
		}
	}

	/**
	 * Loads the config file.
	 *
	 * If a user-specific config file exists, this one is used. Otherwise the (incomplete) standard config file is used.
	 *
	 * @return An {@link Ini} config file
	 */
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

	/**
	 * Creates an {@link EntityManagerFactory} using the connection settings in the specified config file.
	 *
	 * @param config  An {@link Ini} config file containing the database connection settings
	 * @return An EntityManagerFactory used to connect to the iMonDB
	 */
	private EntityManagerFactory getEntityManagerFactory(Ini config) {
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

		return IMonDBManagerFactory.createMySQLFactory(host, port, database, user, password);
	}

	/**
	 * Retrieves the last date from the given config file.
	 *
	 * @param config  An {@link Ini} config file
	 * @return The last date specified in the config file, or 1970-01-01 if not available
	 */
	private Date getLastDate(Ini config) {
		String lastDate = config.get("general", "last_date");

		if(lastDate == null || lastDate.equals("")) {
			logger.info("No cut-off date specified, retrieving all eligible files");
			return new Date(0);
		}
		else {
			try {
				return DATE_FORMAT.parse(lastDate);
			} catch(ParseException e) {
				logger.error("Invalid cut-off date <{}> specified: ", lastDate, e);
				throw new IllegalStateException("Invalid cut-off date specified: " + e);
			}
		}
	}

	/**
	 * Retrieves the run rename mask from the given config file.
	 *
	 * @param config  An {@link Ini} config file
	 * @return The run rename mask specified in the config file, or %p_%dn_%fn if not available
	 */
	private String getRenameMask(Ini config) {
		String renameMask = config.get("general", "rename_run");
		if(renameMask == null || renameMask.equals("")) {
			renameMask = "%p_%dn_%fn";
			logger.info("No run rename mask specified, using default '{}'", renameMask);
		}
		return renameMask;
	}

	private int getNumberOfThreads(Ini config) {
		String nrStr = config.get("general", "num_threads");
		if(nrStr == null || nrStr.equals(""))
			return 1;
		else return Integer.parseInt(nrStr);
	}

	/**
	 * Retrieves the regex used to match the raw files from the given config file.
	 *
	 * @param config  An {@link Ini} config file
	 * @return The regex used to match the raw files specified in the config file
	 */
	private String getMatchFile(Ini config) {
		String matchFile = config.get("general", "match_file");
		if(matchFile == null || matchFile.equals("")) {
			logger.error("The 'match_file' regex must be specified in the config file");
			throw new IllegalStateException("The 'match_file' regex must be specified in the config file");
		}
		return matchFile;
	}
}
