package inspector.jmondb.convert.Thermo;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import inspector.jmondb.convert.InstrumentModel;
import inspector.jmondb.convert.RawFileMetaData;
import inspector.jmondb.model.CV;
import inspector.jmondb.model.Run;
import inspector.jmondb.model.Value;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * An extractor to retrieve instrument data (either status log or tune method data) from Thermo raw files.
 *
 * Attention: instrument data extraction is only possible on a Microsoft Windows platform!
 */
public class ThermoRawFileExtractor {

	protected static final Logger logger = LogManager.getLogger(ThermoRawFileExtractor.class);

	/** static lock to make sure that the Thermo external resources are only accessed by a single instance */
	private static final Lock FILE_COPY_LOCK = new ReentrantLock();

	/** Properties containing a list of value names that have to be excluded */
	private PropertiesConfiguration exclusionProperties;

	//TODO 1: correctly specify the used cv
	//TODO 1: maybe we can even re-use some terms from the PSI-MS cv?
	//TODO 2: this is a global variable to fix a duplicate key error when inserting multiple CV objects with the same label that didn't exist in the database before
	//TODO 2: fix this by having a global CV list or something
	private CV cv = new CV("iMonDB", "Dummy controlled vocabulary containing iMonDB terms", "https://bitbucket.org/proteinspector/jmondb/", "0.0.1");

	/**
	 * Creates an extractor to retrieve instrument data from Thermo raw files.
	 */
	public ThermoRawFileExtractor() {
		// read the exclusion properties
		exclusionProperties = initializeExclusionProperties();

		// make sure the extractor exe's are available outside the jar
		FILE_COPY_LOCK.lock();
		try {
			if(!new File("./Thermo/ThermoMetaData.exe").exists() ||
					!new File("./Thermo/ThermoStatusLog.exe").exists() ||
					!new File("./Thermo/ThermoTuneMethod.exe").exists()) {
				// copy the resources outside the jar
				logger.info("Copying the Thermo extractor CLI's to a new folder in the base directory");
				copyResources(ThermoRawFileExtractor.class.getResource("/Thermo"), new File("./Thermo"));
			}
		} finally {
			FILE_COPY_LOCK.unlock();
		}
	}

	/**
	 * Reads a properties file containing a list of value names that have to be excluded.
	 *
	 * A file with exclusion properties can be provided as command-line argument "-Dexclusion.properties=file-name".
	 * Otherwise, the default exclusion properties are used.
	 *
	 * @return A {@link PropertiesConfiguration} for the exclusion properties
	 */
	private PropertiesConfiguration initializeExclusionProperties() {
		try {
			// check whether the exclusion properties were specified as argument
			String systemProperties = System.getProperty("exclusion.properties");
			if(systemProperties != null) {
				if(!new File(systemProperties).exists()) {
					logger.error("The exclusion properties file <{}> does not exist", systemProperties);
					throw new IllegalArgumentException("The exclusion properties file to read does not exist: " + systemProperties);
				}
				else
					return new PropertiesConfiguration(systemProperties);
			}

			// else load the standard exclusion properties
			return new PropertiesConfiguration(ThermoRawFileExtractor.class.getResource("/exclusion.properties"));

		} catch(ConfigurationException e) {
			logger.error("Error while reading the exclusion properties: {}", e);
			throw new IllegalStateException("Error while reading the exclusion properties: " + e);
		}
	}

	/**
	 * Copies resources to a new destination.
	 *
	 * @param originUrl  The URL where the resources originate
	 * @param destinationDir  The destination directory to which the resources are copied
	 */
	private void copyResources(URL originUrl, File destinationDir) {
		try {
			URLConnection urlConnection = originUrl.openConnection();
			if(urlConnection instanceof JarURLConnection) {	// resources inside a jar file
				copyJarResources((JarURLConnection) urlConnection, destinationDir);
			} else if(urlConnection instanceof FileURLConnection) {	// resources in a folder
				FileUtils.copyDirectory(new File(originUrl.getFile()), destinationDir);
			} else {
				logger.error("Could not copy resources, unknown URLConnection: {}", urlConnection.getClass().getSimpleName());
				throw new IllegalStateException("Unknown URLConnection: " + urlConnection.getClass().getSimpleName());
			}
		} catch(IOException e) {
			logger.error("Could not copy resources: {}", e.getMessage());
			throw new IllegalStateException("Could not copy resources: " + e.getMessage());
		}

	}

	/**
	 * Copies resources from inside a jar file to a new destination.
	 *
	 * This is necessary because the CLI exe's can't be run from inside a packaged jar.
	 *
	 * @param jarConnection  The connection to the resources in the jar file
	 * @param destinationDir  The destination directory to which the resources are copied
	 */
	private void copyJarResources(JarURLConnection jarConnection, File destinationDir) {
		try {
			JarFile jarFile = jarConnection.getJarFile();
			for(Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
				JarEntry entry = entries.nextElement();

				// find all items in the jar that need to be copied
				if(entry.getName().startsWith(jarConnection.getEntryName())) {
					String fileName = StringUtils.removeStart(entry.getName(), jarConnection.getEntryName());

					if(!entry.isDirectory()) {
						// copy each individual file
						InputStream entryInputStream = null;
						try {
							entryInputStream = jarFile.getInputStream(entry);
							FileUtils.copyInputStreamToFile(entryInputStream, new File(destinationDir, fileName));
						} finally {
							if(entryInputStream != null)
								entryInputStream.close();
						}
					} else {
						// create the required directories
						File newDir = new File(destinationDir, fileName);
						if(!newDir.exists() && !newDir.mkdir())
							throw new IOException("Failed to create a new directory: " + newDir.getPath());
					}
				}
			}
		} catch(IOException e) {
			logger.error("Could not copy jar resources: {}", e.getMessage());
			throw new IllegalStateException("Could not copy jar resources: " + e.getMessage());
		}
	}

	/**
	 * Creates a {@link Run} containing as {@link Value}s the status log and tune method data.
	 *
	 * @param fileName  The name of the raw file from which the instrument data will be extracted
	 * @return A Run containing the instrument data as Values
	 */
	public Run extractInstrumentData(String fileName) {
		try {
			// test if the file name is valid
			File rawFile = getFile(fileName);

			// extract raw file meta data
			RawFileMetaData metaData = getMetaData(rawFile);
			Timestamp date = metaData.getDate();
			InstrumentModel model = metaData.getModel();

			// extract the data from the row file
			ArrayList<Value> statusLogValues = getValues(rawFile, model, true);
			ArrayList<Value> tuneMethodValues = getValues(rawFile, model, false);

			// create a run containing all the instrument data values
			String runName = FilenameUtils.getBaseName(rawFile.getName());
			Run run = new Run(runName, rawFile.getCanonicalPath(), date);
			// add the values to the run
			statusLogValues.forEach(run::addValue);
			tuneMethodValues.forEach(run::addValue);

			return run;

		} catch(IOException e) {
			logger.error("Error while resolving the canonical path for file <{}>", fileName);
			throw new IllegalStateException("Error while resolving the canonical path for file <" + fileName + ">");
		}
	}

	/**
	 * Checks whether the given file name is valid and returns a file reference.
	 *
	 * @param fileName  The given file name
	 * @return A reference to the given file
	 */
	private File getFile(String fileName) {
		// check whether the file name is valid
		if(fileName == null) {
			logger.error("Invalid file name <null>");
			throw new NullPointerException("Invalid file name");
		}
		// check whether the file has the correct *.raw extension
		else if(!FilenameUtils.getExtension(fileName).equalsIgnoreCase("raw")) {
			logger.error("Invalid file name <{}>: Not a *.raw file", fileName);
			throw new NullPointerException("Not a *.raw file");
		}

		File file = new File(fileName);
		// check whether the file exists
		if(!file.exists()) {
			logger.error("The raw file <{}> does not exist", file.getAbsolutePath());
			throw new IllegalArgumentException("The raw file to read does not exist: " + file.getAbsolutePath());
		}

		return file;
	}

	/**
	 * Extracts experiment meta data from the raw file, such as the sample date and the instrument model.
	 *
	 * @param rawFile  The raw file from which the instrument data will be read
	 * @return A {@link RawFileMetaData} containing the sample date and the instrument model
	 */
	private RawFileMetaData getMetaData(File rawFile) {
		// execute the CLI process
		Process process = executeProcess("./Thermo/ThermoMetaData.exe", rawFile);

		try {
			// read the CLI output data
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			// the first line contains the experiment date
			Timestamp date = readDate(reader);

			// the second line contains information about the instrument model
			InstrumentModel model = readInstrumentModel(reader);

			// make sure the process has finished
			process.waitFor();
			// close resources
			reader.close();

			return new RawFileMetaData(date, model);

		} catch(IOException e) {
			logger.error("Could not read the raw file extractor output: {}", e.getMessage());
			throw new IllegalStateException("Could not read the raw file extractor output: " + e.getMessage());
		} catch(InterruptedException e) {
			logger.error("Error while extracting the raw file: {}", e);
			throw new IllegalStateException("Error while extracting the raw file: " + e);
		}
	}

	/**
	 * Extracts instrument data from the raw file and computes (summary) statistics for the desired values.
	 *
	 * @param rawFile  The raw file from which the instrument data will be read
	 * @param model  The mass spectrometer {@link InstrumentModel}
	 * @param isStatusLog  True if the status log values have to be generated, false if the tune method values have to be generated
	 * @return A list of the computed instrument data {@link Value}s
	 */
	private ArrayList<Value> getValues(File rawFile, InstrumentModel model, boolean isStatusLog) {
		String cliPath;
		String valueType;
		if(isStatusLog) {
			cliPath = "./Thermo/ThermoStatusLog.exe";
			valueType = "statuslog";
		}
		else {
			cliPath = "./Thermo/ThermoTuneMethod.exe";
			valueType = "tunemethod";
		}

		// execute the CLI process
		Process process = executeProcess(cliPath, rawFile);

		try {
			// read the CLI output data
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			// read all the raw values
			Table<String, String, ArrayList<String>> rawValues = readRawValues(reader, model);

			// make sure the process has finished
			process.waitFor();
			// close resources
			reader.close();

			// filter out unwanted values
			filter(rawValues, valueType);

			// compute the summary statistics
			return computeStatistics(rawValues, valueType);

		} catch(IOException e) {
			logger.error("Could not read the raw file extractor output: {}", e.getMessage());
			throw new IllegalStateException("Could not read the raw file extractor output: " + e.getMessage());
		} catch(InterruptedException e) {
			logger.error("Error while extracting the raw file: {}", e);
			throw new IllegalStateException("Error while extracting the raw file: " + e);
		}
	}

	/**
	 * Starts a process to execute the given C++ exe.
	 *
	 * @param cliPath  The path to the C++ exe that will be executed
	 * @param rawFile  The raw file that will be processed by the C++ exe
	 * @return  A {@link Process} to execute the given C++ exe
	 */
	private Process executeProcess(String cliPath, File rawFile) {
		try {
			// execute the CLI process
			return Runtime.getRuntime().exec(new File(cliPath).getAbsolutePath() + " \"" + rawFile.getAbsoluteFile() + "\"");
		} catch(IOException e) {
			logger.error("Could not execute the raw file extractor: {}", e);
			throw new IllegalStateException("Could not execute the raw file extractor. Are you running this on a Windows platform? " + e);
		}
	}

	/**
	 * Converts an MS CV-term to an instrument model.
	 *
	 * @param reader  A reader that reads as next line the instrument model description
	 * @return The {@link InstrumentModel}
	 * @throws IOException
	 */
	private InstrumentModel readInstrumentModel(BufferedReader reader) throws IOException {

		String modelLine = reader.readLine();
		InstrumentModel model = null;
		if(modelLine != null) {
			String modelCv = modelLine.split("\t")[1];
			//TODO: interpret the PSI-MS OBO file
			switch(modelCv) {
				case "MS:1000449":
					model = InstrumentModel.THERMO_LTQ_ORBITRAP;
					break;
				case "MS:1000556":
					model = InstrumentModel.THERMO_ORBITRAP_XL;
					break;
				case "MS:1000855":
					model = InstrumentModel.THERMO_LTQ_VELOS;
					break;
				case "MS:1001510":
					model = InstrumentModel.THERMO_TSQ_VANTAGE;
					break;
				case "MS:1001742":
					model = InstrumentModel.THERMO_ORBITRAP_VELOS;
					break;
				case "MS:1001911":
					model = InstrumentModel.THERMO_Q_EXACTIVE;
					break;
				case "MS:1002416":
					model = InstrumentModel.THERMO_ORBITRAP_FUSION;
					break;
				default:
					model = InstrumentModel.UNKNOWN_MODEL;
					logger.info("Unknown instrument model <{}>", modelCv);
					break;
			}
		}

		return model;
	}

	/**
	 * Converts the sample date description to a {@link Timestamp}.
	 *
	 * @param reader  A reader that reads as next line the sample date description
	 * @return The sample date
	 * @throws IOException
	 */
	private Timestamp readDate(BufferedReader reader) throws IOException {
		try {
			String dateLine = reader.readLine();
			Timestamp date = null;
			if(dateLine != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss zzz", Locale.US);
				date = new Timestamp(dateFormat.parse(dateLine.split("\t")[1]).getTime());
			}
			return date;

		} catch(ParseException e) {
			logger.error("Error while parsing the date: {}", e.getMessage());
			throw new IllegalStateException("Error while parsing the date: " + e.getMessage());
		}
	}

	/**
	 * Reads the instrument data from the given reader.
	 *
	 * @param reader  A reader to read the instrument data
	 * @param model  The mass spectrometer {@link InstrumentModel}
	 * @return A {@link Table} with as key a possible header and the property name, and a list of values for each property
	 */
	private Table<String, String, ArrayList<String>> readRawValues(BufferedReader reader, InstrumentModel model) {
		try {
			Table<String, String, ArrayList<String>> data = HashBasedTable.create();

			// read all the individual values
			String line;
			String header = "";
			while((line = reader.readLine()) != null) {
				String[] values = line.split("\t");
				// header
				if(values.length == 1 && values[0].length() > 0) {
					switch(model) {
						case THERMO_LTQ_ORBITRAP:
						case THERMO_ORBITRAP_XL:
						case THERMO_LTQ_VELOS:
						case THERMO_ORBITRAP_VELOS:
							header = headerOrbitrap(values[0]);
							break;
						case THERMO_TSQ_VANTAGE:
							header = headerTsqVantage(values[0], header);
							break;
						case THERMO_Q_EXACTIVE:
							header = headerQExactive(values[0]);
							break;
						case THERMO_ORBITRAP_FUSION:
							header = headerOrbitrapFusion(values[0], header);
							break;
						default:
							header = values[0];
							break;
					}
				}
				// value
				else if(values.length == 2) {
					String[] nameValue;
					switch(model) {
						case THERMO_LTQ_ORBITRAP:
						case THERMO_ORBITRAP_XL:
						case THERMO_LTQ_VELOS:
						case THERMO_ORBITRAP_VELOS:
							nameValue = valueOrbitrap(values);
							break;
						case THERMO_TSQ_VANTAGE:
							nameValue = valueTsqVantage(values);
							break;
						case THERMO_Q_EXACTIVE:
						case THERMO_ORBITRAP_FUSION:
							nameValue = valueQExactiveFusion(values);
							break;
						default:
							nameValue = values;
							break;
					}

					// save value
					if(!data.contains(header, nameValue[0]))
						data.put(header, nameValue[0], new ArrayList<>());
					data.get(header, nameValue[0]).add(nameValue[1]);
				}
			}

			return data;

		} catch(IOException e) {
			logger.error("Error while reading the instrument data: {}", e);
			throw new IllegalStateException("Error while reading the instrument data: " + e);
		}
	}

	private String headerOrbitrap(String header) throws UnsupportedEncodingException {
		return new String(header.trim().getBytes("ascii"));
	}

	private String headerTsqVantage(String newHeader, String oldHeader) throws UnsupportedEncodingException {
		newHeader = newHeader.trim();
		if(oldHeader.contains("-"))
			oldHeader = oldHeader.substring(0, oldHeader.indexOf('-')).trim();

		if(newHeader.substring(0, 1).equals("\"") && oldHeader.length() > 0) {
			String result = oldHeader + " - " + newHeader;
			return new String(result.getBytes("ascii"));
		}
		else
			return new String(newHeader.getBytes("ascii"));
	}

	private String headerQExactive(String header) throws UnsupportedEncodingException {
		String result = header.substring(header.indexOf(' '), header.indexOf(':')).trim();
		return new String(result.getBytes("ascii"));
	}

	private String headerOrbitrapFusion(String newHeader, String oldHeader) throws UnsupportedEncodingException {
		if(newHeader.contains(":"))
			return oldHeader;
		else
			return new String(newHeader.trim().getBytes("ascii"));
	}

	private String[] valueOrbitrap(String[] line) throws UnsupportedEncodingException {
		String name = line[0].trim();
		name = name.substring(0, name.lastIndexOf(':'));
		String value = line[1].trim();

		return new String[] { new String(name.getBytes("ascii")), value };
	}

	private String[] valueTsqVantage(String[] line) throws UnsupportedEncodingException {
		return new String[] { new String(line[0].getBytes("ascii")), line[1] };
	}

	private String[] valueQExactiveFusion(String[] line) throws UnsupportedEncodingException {
		String name = line[0].trim();
		if(name.contains(":"))
			name = name.substring(0, name.lastIndexOf(':'));
		String value = line[1].trim();

		return new String[] { new String(name.getBytes("ascii")), value };
	}

	/**
	 * Filters values that are set in the exclusion properties.
	 *
	 * @param data  The data from which values will be removed
	 * @param valueType  The type of values for which the exclusion properties has to be filtered
	 */
	private void filter(Table<String, String, ArrayList<String>> data, String valueType) {
		String[] filterLong = exclusionProperties.getStringArray(valueType + "-long");
		String[] filterShort = exclusionProperties.getStringArray(valueType + "-short");

		// filter out all the entries that have the (exact!) matching long name
		for(String filter : filterLong) {
			String[] filters = filter.split(" - ");
			data.row(filters[0]).remove(filters[1]);
		}
		// filter out all the entries that have a (partially!) matching short name
		//TODO: this is hardly very efficient, can we come up with something better?
		for(Iterator<Table.Cell<String, String, ArrayList<String>>> it = data.cellSet().iterator(); it.hasNext(); ) {
			Table.Cell<String, String, ArrayList<String>> cell = it.next();
			boolean toRemove = false;
			for(int i = 0; i < filterShort.length && !toRemove; i++) {
				toRemove = cell.getColumnKey().contains(filterShort[i]);
			if(toRemove)
				it.remove();
			}
		}
	}

	/**
	 * Calculates properties for each instrument value, including summary statistics if multiple values for the same parameter are present.
	 *
	 * @param data  A Table with as key a possible header and the property name, and a list of values for each property
	 * @return A list of {@link Value}s
	 */
	private ArrayList<Value> computeStatistics(Table<String, String, ArrayList<String>> data, String valueType) {
		ArrayList<Value> values = new ArrayList<>(data.size());

		for(Table.Cell<String, String, ArrayList<String>> cell : data.cellSet()) {
			// calculate the summary value
			Boolean isNumeric = true;
			String firstValue = cell.getValue().get(0);
			Integer n;
			Integer nDiff;
			Double min = null;
			Double max = null;
			Double mean = null;
			Double median = null;
			Double sd = null;
			Double q1 = null;
			Double q3 = null;

			DescriptiveStatistics stats = new DescriptiveStatistics(cell.getValue().size());
			Frequency freq = new Frequency();
			for(int i = 0; i < cell.getValue().size(); i++) {
				String s = cell.getValue().get(i);
				if(s != null && !s.equals("")) {
					freq.addValue(s);
					try {
						stats.addValue(Double.parseDouble(s));
					} catch(NumberFormatException nfe) {
						isNumeric = false;
					}
				}
			}
			n = (int) freq.getSumFreq();
			nDiff = freq.getUniqueCount();
			if(isNumeric) {
				min = stats.getMin();
				max = stats.getMax();
				mean = stats.getMean();
				median = stats.getPercentile(50);
				sd = stats.getStandardDeviation();
				q1 = stats.getPercentile(25);
				q3 = stats.getPercentile(75);
			}

			//TODO: correctly set the accession number once we have a valid cv
			String name = cell.getRowKey() + " - " + cell.getColumnKey();
			String accession = name;
			Value value = new Value(name, valueType, accession, cv, isNumeric, firstValue, n, nDiff, min, max, mean, median, sd, q1, q3);

			values.add(value);
		}

		return values;
	}
}
