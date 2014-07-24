package inspector.jmondb.convert;

import inspector.jmondb.io.IMonDBWriter;
import inspector.jmondb.model.CV;
import inspector.jmondb.model.Run;
import inspector.jmondb.model.Value;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManagerFactory;
import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatusLogWriter {

	private static final Logger logger = LogManager.getLogger(StatusLogWriter.class);

	private Timestamp date;

	private enum InstrumentModel { ORBITRAP_VELOS, ORBITRAP_XL, Q_EXACTIVE, TSQ_VANTAGE, UNKNOWN_MODEL }

	/**
	 * Writes the status log information for the given Thermo raw file to the given project in an iMonDB.
	 *
	 * @param fileName  The file name of the Thermo raw file
	 * @param emf  The EntityManagerFactory representing the iMonDB connection
	 * @param projectLabel  The label of the project to which the run represented by the given raw file belongs
	 */
	public void write(String fileName, EntityManagerFactory emf, String projectLabel) {
		// test if the file name is valid
		File file = getFile(fileName);

		// execute the command-line application to extract the status log
		HashMap<String, ArrayList<String>> data = readStatusLog(file);
		if(data != null) {
			// compute the summary statistics
			ArrayList<Value> values = computeStatistics(data);

			// create a run containing all the values
			String runName = FilenameUtils.getBaseName(file.getName());
			Run run = null;
			try {
				run = new Run(runName, file.getCanonicalPath(), date);
			} catch(IOException e) {
				logger.error("Error while resolving the canonical path for file <{}>", file.getPath());
				throw new IllegalStateException("Error while resolving the canonical path for file <" + file.getPath() + ">");
			}
			values.forEach(run::addValue);

			// store the run in the database
			persist(emf, projectLabel, run);
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
	 * Executes the ThermoStatusLog.exe command line application to extract the status log information from the Thermo
	 * raw file and returns a {@link BufferedReader} to read this information.
	 *
	 * @param file  A reference to the Thermo raw file for which the status log information will be read
	 * @return A HashMap consisting of the status log labels as keys and a list of values for each key
	 */
	private HashMap<String, ArrayList<String>> readStatusLog(File file) {
		HashMap<String, ArrayList<String>> data;

		try {
			// execute the CLI process
			URL statusLogExe = StatusLogWriter.class.getResource("/ThermoStatusLog/ThermoStatusLog.exe");
			Process process = Runtime.getRuntime().exec(statusLogExe.getFile() + " " + file.getAbsoluteFile());

			// read the status log data
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			// read each item on a new line
			// the first line contains information about the instrument model
			String modelLine = reader.readLine();
			InstrumentModel model = null;
			if(modelLine != null) {
				String modelCv = modelLine.split("\t")[1];
				// read the status log data depending on the type of instrument model
				//TODO: interpret the PSI-MS OBO file
				switch(modelCv) {
					case "MS:1001742":
						model = InstrumentModel.ORBITRAP_VELOS;
						break;
					case "MS:1000556":
						model = InstrumentModel.ORBITRAP_XL;
						break;
					case "MS:1001911":
						model = InstrumentModel.Q_EXACTIVE;
						break;
					case "MS:1001510":
						model = InstrumentModel.TSQ_VANTAGE;
						break;
					default:
						model = InstrumentModel.UNKNOWN_MODEL;
						logger.info("Unknown instrument model <{}>", modelCv);
						break;
				}
			}

			// the second line contains the date
			String dateLine = reader.readLine();
			if(dateLine != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss zzz");
				date = new Timestamp(dateFormat.parse(dateLine.split("\t")[1]).getTime());
			}

			// read the remainder of the data
			data = readData(reader, model);

			// make sure the process has finished
			process.waitFor();

		} catch(IOException e) {
			logger.error("Could not execute the status log extractor: {}", e);
			throw new IllegalStateException("Could not execute the status log extractor: " + e);
		} catch(InterruptedException e) {
			logger.error("Error while extracting the status log: {}", e);
			throw new IllegalStateException("Error while extracting the status log: " + e);
		} catch(ParseException e) {
			logger.error("Error while parsing the date: {}", e);
			throw new IllegalStateException("Error while parsing the date: " + e);
		}

		return data;
	}

	/**
	 * Reads the status log data.
	 *
	 * @param reader  BufferedReader to read the status log data
	 * @param model  The instrument model from which the status log was generated
	 * @return A HashMap consisting of the status log labels as keys and a list of values for each key
	 */
	private HashMap<String, ArrayList<String>> readData(BufferedReader reader, InstrumentModel model) {
		HashMap<String, ArrayList<String>> data = new HashMap<>();
		try {
			// read all the individual status log values
			String line;
			String subTitle = "";
			while((line = reader.readLine()) != null) {
				String[] values = line.split("\t");
				// sub title
				if(values.length == 1 && values[0].length() > 0) {
					switch(model) {
						case ORBITRAP_VELOS:
						case ORBITRAP_XL:
							subTitle = subTitleOrbitrap(values[0]);
							break;
						case Q_EXACTIVE:
							subTitle = subTitleQExactive(values[0]);
							break;
						case TSQ_VANTAGE:
							subTitle = "";	// shouldn't come here
							break;
						default:
							subTitle = values[0];
							break;
					}
				}
				// value
				else if(values.length == 2) {
					String[] nameValue;
					switch(model) {
						case ORBITRAP_VELOS:
						case ORBITRAP_XL:
							nameValue = valueOrbitrap(values);
							break;
						case Q_EXACTIVE:
							nameValue = valueQExactive(values);
							break;
						case TSQ_VANTAGE:
							nameValue = valueTsqVantage(values);
							break;
						default:
							nameValue = values;
							break;
					}

					// save value
					String key = subTitle + nameValue[0];
					if(!data.containsKey(key))
						data.put(key, new ArrayList<>());
					data.get(key).add(nameValue[1]);
				}
			}
		} catch(IOException e) {
			logger.error("Error while reading the status log data: {}", e);
			throw new IllegalStateException("Error while reading the status log data: " + e);
		}

		return data;
	}

	private String subTitleOrbitrap(String subTitle) {
		return subTitle.trim() + " - ";
	}

	private String subTitleQExactive(String subTitle) {
		return subTitle.substring(subTitle.indexOf(' '), subTitle.indexOf(':')).trim() + " - ";
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
	 * Computes summary statistics for each status log value.
	 *
	 * @param data  A HashMap consisting of the status log labels as keys and a list of values for each key
	 * @return A list of {@link Value}s
	 */
	private ArrayList<Value> computeStatistics(HashMap<String, ArrayList<String>> data) {
		ArrayList<Value> values = new ArrayList<>(data.size());

		//TODO: correctly specify the used cv
		//TODO: maybe we can even re-use some terms from the PSI-MS cv?
		CV cv = new CV("iMonDB", "Dummy controlled vocabulary containing iMonDB terms", "https://bitbucket.org/proteinspector/jmondb/", "0.0.1");

		for(Map.Entry<String, ArrayList<String>> entry : data.entrySet()) {
			// calculate the summary value
			Boolean isNumeric = true;
			String firstValue = entry.getValue().get(0);
			Integer n = null;
			Integer nDiff = null;
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
			if(isNumeric) {
				min = (float) stats.getMin();
				max = (float) stats.getMax();
				mean = (float) stats.getMean();
				median = (float) stats.getPercentile(50);
				sd = (float) stats.getStandardDeviation();
				q1 = (float) stats.getPercentile(25);
				q3 = (float) stats.getPercentile(75);
			}

			//TODO: correctly set the accession number once we have a valid cv
			Value value = new Value(entry.getKey(), "statuslog", entry.getKey(), cv, isNumeric, firstValue, n, nDiff, nNotMissing, min, max, mean, median, sd, q1, q3);

			values.add(value);
		}

		return values;
	}

	/**
	 * Save the given run to its project in the iMonDB.
	 *
	 * @param emf  The connection to the iMonDB
	 * @param projectLabel  The label of the project to which the given run belongs
	 * @param run  The run with the computed status log values to be persisted in the database
	 */
	private void persist(EntityManagerFactory emf, String projectLabel, Run run) {
		IMonDBWriter writer = new IMonDBWriter(emf);
		writer.writeRun(run, projectLabel);
	}
}
