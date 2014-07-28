package inspector.jmondb.convert.Thermo;

import inspector.jmondb.convert.InstrumentModel;
import inspector.jmondb.model.CV;
import inspector.jmondb.model.Run;
import inspector.jmondb.model.Value;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ThermoRawFileExtractor {

	protected static final Logger logger = LogManager.getLogger(ThermoRawFileExtractor.class);

	private File rawFile;

	private InstrumentModel model;

	private Timestamp date;

	public ThermoRawFileExtractor(String fileName) {
		// test if the file name is valid
		rawFile = getFile(fileName);
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
	 * Creates a {@link Run} containing as {@link Value}s the status log and tune method data.
	 *
	 * @return A Run containing the instrument data as Values
	 */
	public Run extractInstrumentData() {
		try {
			// create a run containing all the instrument data values
			String runName = FilenameUtils.getBaseName(rawFile.getName());
			Run run = new Run(runName, rawFile.getCanonicalPath(), null);	// the date is only set when reading the values

			// add the status log data
			getValues(true).forEach(run::addValue);

			// add the tune method data
			getValues(false).forEach(run::addValue);

			// set the date correctly
			run.setSampleDate(date);

			return run;

		} catch(IOException e) {
			logger.error("Error while resolving the canonical path for file <{}>", rawFile.getPath());
			throw new IllegalStateException("Error while resolving the canonical path for file <" + rawFile.getPath() + ">");
		}
	}

	private ArrayList<Value> getValues(boolean isStatusLog) {
		String cliPath;
		String valueType;
		if(isStatusLog) {
			cliPath = "/Thermo/ThermoStatusLog.exe";
			valueType = "statuslog";
		}
		else {
			cliPath = "/Thermo/ThermoTuneMethod.exe";
			valueType = "tunemethod";
		}

		// execute the command-line application to extract the status log
		HashMap<String, ArrayList<String>> data = readRawFile(cliPath);

		// compute the (summary) statistics
		return computeStatistics(data, valueType);
	}

	/**
	 * Extracts data from the raw file by executing a command-line application and interpreting this output.
	 *
	 * @param cliPath  The path to the specific CLI application used to extract data from the raw file
	 * @return A HashMap with the value name as key and a list of values for each key
	 */
	private HashMap<String, ArrayList<String>> readRawFile(String cliPath) {
		try {
			// execute the CLI process
			URL cliExe = ThermoRawFileExtractor.class.getResource(cliPath);
			Process process = Runtime.getRuntime().exec(cliExe.getFile() + " " + rawFile.getAbsoluteFile());

			// read the CLI output data
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			// the first line contains information about the instrument model
			String modelLine = reader.readLine();
			if(model == null && modelLine != null) {
				String modelCv = modelLine.split("\t")[1];
				//TODO: interpret the PSI-MS OBO file
				switch(modelCv) {
					case "MS:1001742":
						model = InstrumentModel.THERMO_ORBITRAP_VELOS;
						break;
					case "MS:1000556":
						model = InstrumentModel.THERMO_ORBITRAP_XL;
						break;
					case "MS:1001911":
						model = InstrumentModel.THERMO_Q_EXACTIVE;
						break;
					case "MS:1001510":
						model = InstrumentModel.THERMO_TSQ_VANTAGE;
						break;
					default:
						model = InstrumentModel.UNKNOWN_MODEL;
						logger.info("Unknown instrument model <{}>", modelCv);
						break;
				}
			}

			// the second line contains the experiment date
			String dateLine = reader.readLine();
			if(date == null && dateLine != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss zzz", Locale.US);
				date = new Timestamp(dateFormat.parse(dateLine.split("\t")[1]).getTime());
			}

			// read the remainder of the data containing the values
			HashMap<String, ArrayList<String>> data = readData(reader);

			// make sure the process has finished
			process.waitFor();

			return data;

		} catch(IOException e) {
			logger.error("Could not execute the raw file extractor: {}", e);
			throw new IllegalStateException("Could not execute the raw file extractor. Are you running this on a Windows platform? " + e);
		} catch(InterruptedException e) {
			logger.error("Error while extracting the raw file: {}", e);
			throw new IllegalStateException("Error while extracting the raw file: " + e);
		} catch(ParseException e) {
			logger.error("Error while parsing the date: {}", e);
			throw new IllegalStateException("Error while parsing the date: " + e);
		}
	}

	/**
	 * Reads the instrument data from the given reader.
	 *
	 * @param reader  BufferedReader to read the instrument data
	 * @return A HashMap with the value name as key and a list of values for each key
	 */
	private HashMap<String, ArrayList<String>> readData(BufferedReader reader) {
		try {
			HashMap<String, ArrayList<String>> data = new HashMap<>();

			// read all the individual values
			String line;
			String header = "";
			while((line = reader.readLine()) != null) {
				String[] values = line.split("\t");
				// header
				if(values.length == 1 && values[0].length() > 0) {
					switch(model) {
						case THERMO_ORBITRAP_VELOS:
						case THERMO_ORBITRAP_XL:
							header = headerOrbitrap(values[0]);
							break;
						case THERMO_Q_EXACTIVE:
							header = headerQExactive(values[0]);
							break;
						case THERMO_TSQ_VANTAGE:
							header = headerTsqVantage(values[0], header);
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
						case THERMO_ORBITRAP_VELOS:
						case THERMO_ORBITRAP_XL:
							nameValue = valueOrbitrap(values);
							break;
						case THERMO_Q_EXACTIVE:
							nameValue = valueQExactive(values);
							break;
						case THERMO_TSQ_VANTAGE:
							nameValue = valueTsqVantage(values);
							break;
						default:
							nameValue = values;
							break;
					}

					// save value
					String key = header + " - " + nameValue[0];
					if(!data.containsKey(key))
						data.put(key, new ArrayList<>());
					data.get(key).add(nameValue[1]);
				}
			}

			return data;

		} catch(IOException e) {
			logger.error("Error while reading the instrument data: {}", e);
			throw new IllegalStateException("Error while reading the instrument data: " + e);
		}
	}

	private String headerOrbitrap(String header) {
		return header.trim();
	}

	private String headerQExactive(String header) {
		return header.substring(header.indexOf(' '), header.indexOf(':')).trim();
	}

	private String headerTsqVantage(String newHeader, String oldHeader) {
		newHeader = newHeader.trim();
		if(oldHeader.contains("-"))
			oldHeader = oldHeader.substring(0, oldHeader.indexOf('-')).trim();

		if(newHeader.substring(0, 1).equals("\"") && oldHeader.length() > 0)
			return oldHeader + " - " + newHeader;
		else
			return newHeader;
	}

	private String[] valueOrbitrap(String[] line) {
		String name = line[0].trim();
		name = name.substring(0, name.lastIndexOf(':'));
		String value = line[1].trim();

		return new String[] { name, value };
	}

	private String[] valueQExactive(String[] line) {
		String name = line[0].trim();
		if(name.contains(":"))
			name = name.substring(0, name.lastIndexOf(':'));
		String value = line[1].trim();

		return new String[] { name, value };
	}

	private String[] valueTsqVantage(String[] line) {
		return line;
	}

	/**
	 * Calculates properties for each instrument value, including summary statistics if multiple values for the same parameter are present.
	 *
	 * @param data  A HashMap with the value name as key and a list of values for each key
	 * @return A list of {@link Value}s
	 */
	private ArrayList<Value> computeStatistics(HashMap<String, ArrayList<String>> data, String valueType) {
		ArrayList<Value> values = new ArrayList<>(data.size());

		//TODO: correctly specify the used cv
		//TODO: maybe we can even re-use some terms from the PSI-MS cv?
		CV cv = new CV("iMonDB", "Dummy controlled vocabulary containing iMonDB terms", "https://bitbucket.org/proteinspector/jmondb/", "0.0.1");

		for(Map.Entry<String, ArrayList<String>> entry : data.entrySet()) {
			// calculate the summary value
			Boolean isNumeric = true;
			String firstValue = entry.getValue().get(0);
			Integer n;
			Integer nDiff;
			Integer nNotMissing = 0;
			Float min = null;
			Float max = null;
			Float mean = null;
			Float median = null;
			Float sd = null;
			Float q1 = null;
			Float q3 = null;

			DescriptiveStatistics stats = new DescriptiveStatistics(entry.getValue().size());
			Frequency freq = new Frequency();
			for(int i = 0; i < entry.getValue().size(); i++) {
				String s = entry.getValue().get(i);
				if(s != null && !s.equals("")) {
					nNotMissing++;
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
			if(isNumeric && n > 1) {
				min = (float) stats.getMin();
				max = (float) stats.getMax();
				mean = (float) stats.getMean();
				median = (float) stats.getPercentile(50);
				sd = (float) stats.getStandardDeviation();
				q1 = (float) stats.getPercentile(25);
				q3 = (float) stats.getPercentile(75);
			}

			//TODO: correctly set the accession number once we have a valid cv
			Value value = new Value(entry.getKey(), valueType, entry.getKey(), cv, isNumeric, firstValue, n, nDiff, nNotMissing, min, max, mean, median, sd, q1, q3);

			values.add(value);
		}

		return values;
	}
}
