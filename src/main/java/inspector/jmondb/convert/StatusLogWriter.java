package inspector.jmondb.convert;

import inspector.jmondb.io.IMonDBReader;
import inspector.jmondb.io.IMonDBWriter;
import inspector.jmondb.model.Property;
import inspector.jmondb.model.Value;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManagerFactory;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StatusLogWriter {

	private static final Logger logger = LogManager.getLogger(StatusLogWriter.class);

	/**
	 * Writes the status log information for the given Thermo raw file to the given IMonDB.
	 *
	 * @param fileName  The file name of the Thermo raw file
	 * @param emf  The EntityManagerFactory representing the IMonDB connection
	 */
	public void write(String fileName, EntityManagerFactory emf) {
		try {
			// test if the file name is valid
			File file = getFile(fileName);

			// execute the command-line application to extract the status log
			BufferedReader reader = getStatusLogReader(file);

			// read the status log
			HashMap<String, ArrayList<String>> data = null;
			// read each item on a new line
			// the first line contains information about the instrument model
			String model = reader.readLine();
			if(model != null) {
				String modelCv = model.split("\t")[1];
				// read the status log data depending on the type of instrument model
				//TODO: interpret the OBO file
				if(modelCv.equals("MS:1001742")) {	// LTQ Orbitrap Velos
					data = readOrbitrapVelos(reader);
				}
				//TODO: else -> other models
			}

			ArrayList<Property> properties = null;
			if(data != null) {
				// compute the summary statistics
				properties = computeStatistics(data);
				// store the data in the database
				IMonDBWriter writer = new IMonDBWriter(emf);
				properties.forEach(writer::writeProperty);
			}

		} catch(IOException e) {
			logger.info("Could not execute the status log extractor: {}", e);
			throw new IllegalStateException("Could not execute the status log extractor: " + e);
		} catch(InterruptedException e) {
			logger.info("Error while extracting the status log: {}", e);
			throw new IllegalStateException("Error while extracting the status log: " + e);
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
		else if(!FilenameUtils.getExtension(fileName).equals("raw")) {
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
	 * @return A BufferedReader reading the status log information
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private BufferedReader getStatusLogReader(File file) throws IOException, InterruptedException {
		URL statusLogExe = StatusLogWriter.class.getResource("/ThermoStatusLog/ThermoStatusLog.exe");
		Process process = Runtime.getRuntime().exec(statusLogExe.getFile() + " " + file.getAbsoluteFile());
		process.waitFor();
		return new BufferedReader(new InputStreamReader(process.getInputStream()));
	}

	/**
	 * Reads the status log data formatted in the LTQ Ortbitrap Velos format.
	 *
	 * @param reader  BufferedReader to read the status log data
	 * @return A HashMap consisting of the status log labels as keys and a list of values for each key
	 */
	private HashMap<String, ArrayList<String>> readOrbitrapVelos(BufferedReader reader) {
		HashMap<String, ArrayList<String>> data = new HashMap<>();
		try {
			// read all the individual status log values
			String line;
			String subTitle = "";
			while((line = reader.readLine()) != null) {
				String[] values = line.split("\t");
				// subtitle
				if(values.length == 1) {
					subTitle = values[0].trim() + " - ";
				}
				// value
				else if(values.length == 2) {
					String name = values[0].trim();
					name = name.substring(0, name.lastIndexOf(':'));
					String value = values[1].trim();
					// save value
					String key = subTitle + name;
					if(!data.containsKey(key))
						data.put(key, new ArrayList<>());
					data.get(key).add(value);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	/**
	 * Computes summary statistics for each status log value.
	 *
	 * @param data  A HashMap consisting of the status log labels as keys and a list of values for each key
	 * @return A list of {@link Property}s with their corresponding {@link Value}s
	 */
	private ArrayList<Property> computeStatistics(HashMap<String, ArrayList<String>> data) {
		ArrayList<Property> properties = new ArrayList<>(data.size());

		for(Map.Entry<String, ArrayList<String>> entry : data.entrySet()) {
			// create a Property
			Property property = new Property(entry.getKey(), "statuslog");
			// add the Value for the property
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

			Value value = new Value(isNumeric, firstValue, n, nDiff, nNotMissing, min, max, mean, median, sd, q1, q3);
			property.addValue(value);

			properties.add(property);

			//TODO: DEBUG
			System.out.println(property.getName() + "\t" + value);
		}

		return properties;
	}
}
