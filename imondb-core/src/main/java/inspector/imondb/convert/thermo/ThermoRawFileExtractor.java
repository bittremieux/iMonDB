package inspector.imondb.convert.thermo;

/*
 * #%L
 * iMonDB Core
 * %%
 * Copyright (C) 2014 - 2015 InSPECtor
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import inspector.imondb.convert.RawFileMetadata;
import inspector.imondb.convert.thermo.instrumentreader.*;
import inspector.imondb.model.*;
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
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
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

    private static final Logger LOGGER = LogManager.getLogger(ThermoRawFileExtractor.class);

    /** static lock to make sure that the Thermo external resources are only accessed by a single instance */
    private static final Lock FILE_COPY_LOCK = new ReentrantLock();

    /** properties containing a list of value names that have to be excluded */
    private PropertiesConfiguration exclusionProperties;

    private static final String EXE_TEXT_ENCODING = "Cp1252";

    //TODO: correctly specify the used cv
    //TODO: maybe we can even re-use some terms from the PSI-MS cv?
    private static CV cvIMon = new CV("iMonDB", "Dummy controlled vocabulary containing iMonDB terms", "https://bitbucket.org/proteinspector/imondb/", "0.0.1");
    private static CV cvMS = new CV("MS", "PSI-MS CV", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", "3.68.0");

    /**
     * Creates an extractor to retrieve instrument data from Thermo raw files.
     */
    public ThermoRawFileExtractor() {
        // read the exclusion properties
        exclusionProperties = initializeExclusionProperties();

        // make sure the extractor exe's are available outside the jar
        try {
            FILE_COPY_LOCK.lock();
            if(!new File("./Thermo/ThermoMetaData.exe").exists()
                    || !new File("./Thermo/ThermoStatusLog.exe").exists()
                    || !new File("./Thermo/ThermoTuneMethod.exe").exists()) {
                // copy the resources outside the jar
                LOGGER.debug("Copying the Thermo extractor CLI's to a new folder in the base directory");
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
                    LOGGER.error("The exclusion properties file <{}> does not exist", systemProperties);
                    throw new IllegalArgumentException("The exclusion properties file to read does not exist: " + systemProperties);
                } else {
                    return new PropertiesConfiguration(systemProperties);
                }
            }

            // else load the standard exclusion properties
            return new PropertiesConfiguration(ThermoRawFileExtractor.class.getResource("/exclusion.properties"));

        } catch(ConfigurationException e) {
            LOGGER.error("Error while reading the exclusion properties: {}", e);
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
            if(urlConnection instanceof JarURLConnection) {
                // resources inside a jar file
                copyJarResources((JarURLConnection) urlConnection, destinationDir);
            } else if(urlConnection instanceof FileURLConnection) {
                // resources in a folder
                FileUtils.copyDirectory(new File(originUrl.getFile()), destinationDir);
            } else {
                LOGGER.error("Could not copy resources, unknown URLConnection: {}", urlConnection.getClass().getSimpleName());
                throw new IllegalStateException("Unknown URLConnection: " + urlConnection.getClass().getSimpleName());
            }
        } catch(IOException e) {
            LOGGER.error("Could not copy resources: {}", e.getMessage());
            throw new IllegalStateException("Could not copy resources: " + e.getMessage(), e);
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
                            if(entryInputStream != null) {
                                entryInputStream.close();
                            }
                        }
                    } else {
                        // create the required directories
                        File newDir = new File(destinationDir, fileName);
                        if(!newDir.exists() && !newDir.mkdir()) {
                            throw new IOException("Failed to create a new directory: " + newDir.getPath());
                        }
                    }
                }
            }
        } catch(IOException e) {
            LOGGER.error("Could not copy jar resources: {}", e.getMessage());
            throw new IllegalStateException("Could not copy jar resources: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a {@link Run} containing as {@link Value}s the status log and tune method data.
     *
     * @param fileName  the name of the raw file from which the instrument data will be extracted, not {@code null}
     * @param runName  the name of the created {@code Run}, if {@code null} the base file name is used
     * @param instrumentName  the name of the {@link Instrument} on which the {@code Run} was performed, not {@code null}
     * @return a {@code Run} containing the instrument data as {@code Value}s
     */
    public Run extractInstrumentData(String fileName, String runName, String instrumentName) {
        try {
            // test if the file name is valid
            File rawFile = getFile(fileName);

            // extract raw file meta data
            RawFileMetadata metadata = getMetadata(rawFile);
            Timestamp date = metadata.getDate();
            InstrumentModel model = metadata.getModel();

            // create the instrument on which the run was performed
            Instrument instrument = new Instrument(instrumentName, model, cvMS);
            // create a run to store all the instrument data values
            Run run = new Run(runName == null ? FilenameUtils.getBaseName(rawFile.getName()) : runName,
                    rawFile.getCanonicalPath(), date, instrument);

            // extract the data from the raw file and add the values to the run
            extractAndAddValues(rawFile, model, true, run);
            extractAndAddValues(rawFile, model, false, run);

            return run;

        } catch(IOException e) {
            LOGGER.warn("Error while resolving the canonical path for file <{}>", fileName);
            throw new IllegalStateException("Error while resolving the canonical path for file <" + fileName + ">", e);
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
            LOGGER.error("Invalid file name <null>");
            throw new NullPointerException("Invalid file name");
            // check whether the file has the correct *.raw extension
        } else if(!"raw".equalsIgnoreCase(FilenameUtils.getExtension(fileName))) {
            LOGGER.error("Invalid file name <{}>: Not a *.raw file", fileName);
            throw new IllegalArgumentException("Not a *.raw file");
        }

        File file = new File(fileName);
        // check whether the file exists
        if(!file.exists()) {
            LOGGER.error("The raw file <{}> does not exist", file.getAbsolutePath());
            throw new IllegalArgumentException("The raw file to read does not exist: " + file.getAbsolutePath());
        }

        return file;
    }

    /**
     * Extracts experiment meta data from the raw file, such as the sample date and the instrument model.
     *
     * @param rawFile  the raw file from which the instrument data will be read, not {@code null}
     * @return {@link RawFileMetadata} information containing the sample date and the instrument model
     */
    private RawFileMetadata getMetadata(File rawFile) {
        // execute the CLI process
        Process process = executeProcess("./Thermo/ThermoMetaData.exe", rawFile);

        try {
            // read the CLI output data
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName(EXE_TEXT_ENCODING)));

            // the first line contains the experiment date
            Timestamp date = readDate(reader);

            // the second line contains information about the instrument model
            InstrumentModel model = readInstrumentModel(reader);

            // make sure the process has finished
            process.waitFor();
            // close resources
            reader.close();

            return new RawFileMetadata(date, model);

        } catch(IOException e) {
            LOGGER.error("Could not read the raw file extractor output: {}", e.getMessage());
            throw new IllegalStateException("Could not read the raw file extractor output: " + e.getMessage(), e);
        } catch(InterruptedException e) {
            LOGGER.error("Error while extracting the raw file: {}", e.getMessage());
            throw new IllegalStateException("Error while extracting the raw file: " + e.getMessage(), e);
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
        } else {
            cliPath = "./Thermo/ThermoTuneMethod.exe";
            valueType = "tunemethod";
        }

        // execute the CLI process
        Process process = executeProcess(cliPath, rawFile);

        try {
            // read the CLI output data
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName(EXE_TEXT_ENCODING)));

            // read all the raw values
            Table<String, String, ArrayList<String>> rawValues = readRawValues(reader, getInstrumentReader(model));

            // make sure the process has finished
            process.waitFor();
            // close resources
            reader.close();

            // filter out unwanted values
            filter(rawValues, valueType);

            // compute the summary statistics and store the values in the given run
            addStatisticsToRun(rawValues, valueType, run);

        } catch(IOException e) {
            LOGGER.error("Could not read the raw file extractor output: {}", e.getMessage());
            throw new IllegalStateException("Could not read the raw file extractor output: " + e.getMessage(), e);
        } catch(InterruptedException e) {
            LOGGER.error("Error while extracting the raw file: {}", e.getMessage());
            throw new IllegalStateException("Error while extracting the raw file: " + e.getMessage(), e);
        }
    }

    /**
     * Creates an {@link InstrumentReader} for the given {@link InstrumentModel}.
     *
     * @param model  the {@code InstrumentModel} for which an {@code InstrumentReader} is created
     * @return the {@code InstrumentReader} for the given {@code InstrumentModel}
     */
    private InstrumentReader getInstrumentReader(InstrumentModel model) {
        switch(model) {
            case THERMO_LTQ:
            case THERMO_LTQ_ORBITRAP:
            case THERMO_ORBITRAP_XL:
            case THERMO_LTQ_VELOS:
            case THERMO_ORBITRAP_VELOS:
                return new OrbitrapReader(EXE_TEXT_ENCODING);
            case THERMO_TSQ_VANTAGE:
                return new TsqVantageReader(EXE_TEXT_ENCODING);
            case THERMO_Q_EXACTIVE:
                return new QExactiveReader(EXE_TEXT_ENCODING);
            case THERMO_ORBITRAP_FUSION:
                return new FusionReader(EXE_TEXT_ENCODING);
            case UNKNOWN_MODEL:
            default:
                return new DefaultInstrumentReader(EXE_TEXT_ENCODING);
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
            LOGGER.error("Could not execute the raw file extractor: {}", e.getMessage());
            throw new IllegalStateException("Could not execute the raw file extractor. Are you running this on a Windows platform? " + e.getMessage(), e);
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
        return modelLine != null ? InstrumentModel.fromString(modelLine.split("\t")[1]) : null;
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
            LOGGER.error("Error while parsing the date: {}", e.getMessage());
            throw new IllegalStateException("Error while parsing the date: " + e.getMessage(), e);
        }
    }

    /**
     * Reads the instrument data from the given reader.
     *
     * @param reader  a {@link BufferedReader} to read the instrument data, not {@code null}
     * @param instrumentReader  an {@link InstrumentReader} used to parse the instrument settings
     * @return a {@link Table} with as key a possible header and the property name, and a list of values for each property
     */
    private Table<String, String, ArrayList<String>> readRawValues(BufferedReader reader, InstrumentReader instrumentReader) {
        try {
            Table<String, String, ArrayList<String>> data = HashBasedTable.create();

            // read all the individual values
            String line;
            // null header not allowed for insertion in the Table
            String header = "";
            while((line = reader.readLine()) != null) {
                if(isSeparator(line)) {
                    // reset header
                    header = "";
                } else if(instrumentReader.isHeader(line)) {
                    // get the header
                    header = instrumentReader.getHeader(line.trim(), header);
                } else {
                    // extract the value
                    String[] nameValue = instrumentReader.getNameAndValue(line.trim());

                    // save the value
                    if(!data.contains(header, nameValue[0])) {
                        data.put(header, nameValue[0], new ArrayList<>());
                    }
                    data.get(header, nameValue[0]).add(nameValue[1]);
                }
            }

            return data;

        } catch(IOException e) {
            LOGGER.error("Error while reading the instrument data: {}", e.getMessage());
            throw new IllegalStateException("Error while reading the instrument data: " + e.getMessage(), e);
        }
    }

    /**
     * Specifies whether the line indicates a separator (i.e. to indicate a new scan or a new segment).
     *
     * @param line  the line that will be checked for being a separator
     * @return  {@code true} if the line indicates a separator, {@code false} if not
     */
    private boolean isSeparator(String line) {
        return line.trim().isEmpty() || line.startsWith("--END_OF_");
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
                if(toRemove) {
                    it.remove();
                }
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
            boolean isEmpty = true;
            for(int i = 0; i < cell.getValue().size(); i++) {
                String s = cell.getValue().get(i);
                if(s != null) {
                    freq.addValue(s);
                    isEmpty &= s.isEmpty();
                    if(isNumeric && !s.isEmpty()) {
                        try {
                            stats.addValue(Double.parseDouble(s));
                        } catch(NumberFormatException nfe) {
                            isNumeric = false;
                        }
                    }
                }
            }
            // add a new value if it has at least one non-empty observation
            if(!isEmpty) {
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

                //TODO: correctly set the accession number once we have a valid cvIMon
                String name = cell.getRowKey().isEmpty() ? cell.getColumnKey() : cell.getRowKey() + " - " + cell.getColumnKey();
                String accession = name;
                Property property = new Property(name, valueType, accession, cvIMon, isNumeric);
                // values are automatically added to the run and the property
                new Value(firstValue, n, nDiff, min, max, mean, median, sd, q1, q3, property, run);
            }
        }
    }
}
