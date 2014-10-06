package inspector.jmondb.convert.Thermo;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import inspector.jmondb.model.InstrumentModel;
import inspector.jmondb.convert.RawFileMetaData;
import inspector.jmondb.model.*;
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
 * For more information on the required operating system and available libraries, please check the official website.
 */
public class ThermoRawFileExtractor {

	protected static final Logger logger = LogManager.getLogger(ThermoRawFileExtractor.class);

	/** static lock to make sure that the Thermo external resources are only accessed by a single instance */
	private static final Lock FILE_COPY_LOCK = new ReentrantLock();

	/** properties containing a list of value names that have to be excluded */
	private PropertiesConfiguration exclusionProperties;

	//TODO 1: correctly specify the used cv
	//TODO 1: maybe we can even re-use some terms from the PSI-MS cv?
	//TODO 2: this is a global variable to fix a duplicate key error when inserting multiple CV objects with the same label that didn't exist in the database before
	//TODO 2: fix this by having a global CV list or something
	private CV cv = new CV("iMonDB", "Dummy controlled vocabulary containing iMonDB terms", "https://bitbucket.org/proteinspector/jmondb/", "0.0.1");
	private CV cvMS = new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0");

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
	 * @return a {@link PropertiesConfiguration} for the exclusion properties
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
	 * @param originUrl  the {@link URL} where the resources originate, not {@code null}
	 * @param destinationDir  the destination directory to which the resources are copied, not {@code null}
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
	 * @param jarConnection  the connection to the resources in the jar file, not {@code null}
	 * @param destinationDir  the destination directory to which the resources are copied, not {@code null}
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
	 * @param fileName  the name of the raw file from which the instrument data will be extracted, not {@code null}
	 * @param runName  the name of the created {@code Run}, if {@code null} the base file name is used
	 * @return a {@code Run} containing the instrument data as {@code Value}s
	 */
	public Run extractInstrumentData(String fileName, String runName) {
		try {
			// test if the file name is valid
			File rawFile = getFile(fileName);

			// extract raw file meta data
			RawFileMetaData metaData = getMetaData(rawFile);
			Timestamp date = metaData.getDate();
			InstrumentModel model = metaData.getModel();

			// create the instrument on which the run was performed
			//TODO: get instrument name (from where?)
			Instrument instrument = new Instrument("my instrument name", model, cvMS);
			// create a run to store all the instrument data values
			if(runName == null)
				runName = FilenameUtils.getBaseName(rawFile.getName());
			Run run = new Run(runName, rawFile.getCanonicalPath(), date, instrument);

			// extract the data from the raw file and add the values to the run
			extractAndAddValues(rawFile, model, true, run);
			extractAndAddValues(rawFile, model, false, run);

			return run;

		} catch(IOException e) {
			logger.error("Error while resolving the canonical path for file <{}>", fileName);
			throw new IllegalStateException("Error while resolving the canonical path for file <" + fileName + ">");
		}
	}

	/**
	 * Checks whether the given file name is valid and returns a file reference.
	 *
	 * @param fileName  the given file name, not {@code null}
	 * @return a reference to the given {@link File}
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
	 * @param rawFile  the raw file from which the instrument data will be read, not {@code null}
	 * @return {@link RawFileMetaData} information containing the sample date and the instrument model
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
	 * @param rawFile  the raw file from which the instrument data will be read, not {@code null}
	 * @param model  the mass spectrometer {@link InstrumentModel}, not {@code null}
	 * @param isStatusLog  {@code true} if the status log values have to be generated, {@code false} if the tune method values have to be generated
	 * @param run  the {@link Run} to which the {@code Value}s will be added, not {@code null}
	 */
	private void extractAndAddValues(File rawFile, InstrumentModel model, boolean isStatusLog, Run run) {
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

			// compute the summary statistics and store the values in the given run
			addStatisticsToRun(rawValues, valueType, run);

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
	 * @param cliPath  the path to the C++ exe that will be executed, not {@code null}
	 * @param rawFile  the raw file that will be processed by the C++ exe, not {@code null}
	 * @return  a {@link Process} to execute the given C++ exe
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
	 * Converts an MS CV-term to an {@link InstrumentModel}.
	 *
	 * @param reader  a {@link BufferedReader} that reads as next line the instrument model description, not {@code null}
	 * @return the {@code InstrumentModel}
	 * @throws IOException
	 */
	private InstrumentModel readInstrumentModel(BufferedReader reader) throws IOException {

		String modelLine = reader.readLine();
		if(modelLine != null)
			return InstrumentModel.fromString(modelLine.split("\t")[1]);

		return null;
	}

	/**
	 * Converts the sample date description to a {@link Timestamp}.
	 *
	 * @param reader  a {@link BufferedReader} that reads as next line the sample date description, not {@code null}
	 * @return the sample date
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
	 * @param reader  a {@link BufferedReader} to read the instrument data, not {@code null}
	 * @param model  the mass spectrometer {@link InstrumentModel}, not {@code null}
	 * @return a {@link Table} with as key a possible header and the property name, and a list of values for each property
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
	 * Filters data that is indicated in the exclusion properties.
	 *
	 * @param data  the data from which indicated values will be removed, not {@code null}
	 * @param valueType  the type of values for which the exclusion properties will be applied, not {@code null}
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
	 * Computes summary statistics for each instrument value.
	 *
	 * @param data  a {@link Table} with as key a possible header and the property name, and a list of values for each property, not {@code null}
	 * @param run  the {@link Run} to which the computed {@code Value}s will be added, not {@code null}
	 */
	private void addStatisticsToRun(Table<String, String, ArrayList<String>> data, String valueType, Run run) {

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
			Property property = new Property(name, valueType, accession, cv, isNumeric);
			// values are automatically added to the run and the property
			new Value(firstValue, n, nDiff, min, max, mean, median, sd, q1, q3, property, run);
		}
	}
}
