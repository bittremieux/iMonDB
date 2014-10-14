package inspector.jmondb.config;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Retrieve information from a YAML config file.
 */
public class ConfigFile {

	protected static final Logger logger = LogManager.getLogger(ConfigFile.class);

	/** Date formatter to convert to and from date strings */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	/** {@link Yaml} object to load and write YAML files */
	private Yaml yaml;

	/** the YAML config file represented as nested {@link Map}s and {@link List}s */
	private Map<String, Object> rootMap;

	/**
	 * Load config information from the given {@link InputStream} representing a YAML config file.
	 *
	 * @param inputStream  the {@code InputStream} representing a YAML config file
	 */
	public ConfigFile(InputStream inputStream) {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setIndent(4);

		yaml = new Yaml(options);

		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) yaml.load(inputStream);
		rootMap = map;
	}

	/**
	 * Write the config information to file "config.yaml".
	 *
	 * <em>Attention:</em> All comments will be lost.
	 */
	public void store() {
		try {
			yaml.dump(rootMap, new FileWriter(new File("config.yaml")));
		} catch(IOException e) {
			logger.error("Error while writing the config file: {}", e.getMessage());
		}
	}

	public String getDatabaseHost() {
		@SuppressWarnings("unchecked")
		String result = ((Map<String, String>) rootMap.get("sql")).get("host");
		return result;
	}

	public String getDatabasePort() {
		@SuppressWarnings("unchecked")
		String result = Integer.toString(((Map<String, Integer>) rootMap.get("sql")).get("port"));
		return result;
	}

	public String getDatabaseName() {
		@SuppressWarnings("unchecked")
		String name = ((Map<String, String>) rootMap.get("sql")).get("database");

		if(name == null) {
			logger.error("The MySQL database name must be specified in the config file");
			throw new IllegalStateException("The MySQL database name must be specified in the config file");
		}
		else
			return name;
	}

	public String getDatabaseUser() {
		@SuppressWarnings("unchecked")
		String user = ((Map<String, String>) rootMap.get("sql")).get("user");

		if(user == null) {
			logger.error("The MySQL user name must be specified in the config file");
			throw new IllegalStateException("The MySQL user name must be specified in the config file");
		}
		else
			return user;
	}

	public String getDatabasePassword() {
		@SuppressWarnings("unchecked")
		String result = ((Map<String, String>) rootMap.get("sql")).get("password");
		return result;
	}

	public File getStartDirectory() {
		@SuppressWarnings("unchecked")
		String dirStr = ((Map<String, String>) rootMap.get("general")).get("dir");

		if(dirStr == null) {
			logger.error("The start directory must be specified in the config file");
			throw new IllegalStateException("The start directory must be specified in the config file");
		}
		else {
			File dir = new File(dirStr);
			if(!dir.exists() || !dir.isDirectory()) {
				logger.error("Path <{}> does not exist or is not a valid directory", dirStr);
				throw new IllegalArgumentException("Path <" + dirStr + "> does not exist or is not a valid directory");
			}
			else
				return dir;
		}
	}

	public Timestamp getLastDate() {
		@SuppressWarnings("unchecked")
		String dateStr = ((Map<String, String>) rootMap.get("general")).get("last_date");

		if(dateStr == null) {
			logger.info("No cut-off date specified, retrieving all eligible files");
			return new Timestamp(new Date(0).getTime());
		}
		else {
			try {
				return new Timestamp(DATE_FORMAT.parse(dateStr).getTime());
			} catch(ParseException e) {
				logger.error("Invalid cut-off date <{}> specified: ", dateStr, e.getMessage());
				throw new IllegalStateException("Invalid cut-off date specified: " + e.getMessage());
			}
		}
	}

	public void setLastDate(Timestamp date) {
		@SuppressWarnings("unchecked")
		Map<String, String> generalMap = (Map<String, String>) rootMap.get("general");
		generalMap.put("last_date", DATE_FORMAT.format(date));
	}

	public String getMatchFile() {
		@SuppressWarnings("unchecked")
		String regexStr = ((Map<String, String>) rootMap.get("general")).get("match_file");

		if(regexStr == null) {
			logger.error("The regex to match file names must be specified in the config file");
			throw new IllegalStateException("The regex to match file names must be specified in the config file");
		}
		else
			return regexStr;
	}

	public int getNumberOfThreads() {
		@SuppressWarnings("unchecked")
		Integer numThreads = ((Map<String, Integer>) rootMap.get("general")).get("num_threads");

		if(numThreads == null || numThreads <= 0) {
			logger.info("Invalid/unknown number of threads specified, defaulting to 1");
			return 1;
		}
		else
			return numThreads;
	}

	public List<Map<String, String>> getInstruments() {
		@SuppressWarnings("unchecked")
		List<Map<String, String>> result =(List<Map<String, String>>) rootMap.get("instruments");
		return result;
	}

	public String getInstrumentNameForFile(File file) {
		String fileName = file.getName();
		String filePath;
		try {
			filePath = FilenameUtils.getFullPath(file.getCanonicalPath());
		} catch(IOException e) {
			logger.error("Error while evaluating the file path: {}", e.getMessage());
			throw new IllegalArgumentException("Error while evaluating the file path: " + e.getMessage());
		}

		@SuppressWarnings("unchecked")
		List<Map<String, String>> instruments = (List<Map<String, String>>) rootMap.get("instruments");
		for(Map<String, String> instrument : instruments) {
			if((instrument.get("regex-source").equals("name") && fileName.matches(instrument.get("regex")))
				|| (instrument.get("regex-source").equals("path") && filePath.matches(instrument.get("regex"))))
				return instrument.get("name");
		}

		logger.info("No instrument found for file <{}>, please check the config file", fileName);
		return null;
	}

	public MetadataMapper getMetadataMapper() {
		@SuppressWarnings("unchecked")
		List<Map<String, String>> metadataMap = (List<Map<String, String>>) rootMap.get("metadata");
		if(metadataMap != null)
			return new MetadataMapper(metadataMap);
		else
			return null;
	}
}
