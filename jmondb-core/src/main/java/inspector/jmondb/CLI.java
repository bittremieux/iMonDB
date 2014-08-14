package inspector.jmondb;

import inspector.jmondb.convert.Thermo.ThermoRawFileExtractor;
import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBWriter;
import inspector.jmondb.model.Run;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManagerFactory;


public class CLI {

	protected static final Logger logger = LogManager.getLogger(CLI.class);

	public static void main(String[] args) {

		EntityManagerFactory emf = null;

		// create commandline options
		Options options = createOptions();

		// parse arguments
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine cmd = parser.parse(options, args);

			// help
			if(cmd.hasOption("?"))
				new HelpFormatter().printHelp("jMonDB-core", options);
			else {
				String host = null;
				String port = null;
				String database = null;
				String user = null;
				String pass = null;
				if(cmd.hasOption("h"))
					host = cmd.getOptionValue("h");
				if(cmd.hasOption("p"))
					port = cmd.getOptionValue("p");
				if(cmd.hasOption("db"))
					database = cmd.getOptionValue("db");
				else {
					logger.error("No database provided");
					System.err.println("No database provided");
					new HelpFormatter().printHelp("jMonDB-core", options);
				}
				if(cmd.hasOption("u"))
					user = cmd.getOptionValue("u");
				else {
					logger.error("No user name provided");
					System.err.println("No user name provided");
					new HelpFormatter().printHelp("jMonDB-core", options);
				}
				if(cmd.hasOption("pw"))
					pass = cmd.getOptionValue("pw");

				// create database connection
				emf = IMonDBManagerFactory.createMySQLFactory(host, port, database, user, pass);
				IMonDBWriter writer = new IMonDBWriter(emf);

				// store raw file in the database
				String rawFile = null;
				String projectLabel = null;
				if(cmd.hasOption("f"))
					rawFile = cmd.getOptionValue("f");
				else {
					logger.error("No raw file provided");
					System.err.println("No raw file provided");
					new HelpFormatter().printHelp("jMonDB-core", options);
				}
				if(cmd.hasOption("pr"))
					projectLabel = cmd.getOptionValue("pr");
				else {
					logger.error("No project label provided");
					System.err.println("No project label provided");
					new HelpFormatter().printHelp("jMonDB-core", options);
				}
				Run run = new ThermoRawFileExtractor().extractInstrumentData(rawFile);
				writer.writeRun(run, projectLabel);
			}

		} catch (ParseException e) {
			logger.error("Error while parsing the command-line arguments: {}", e.getMessage());
			System.err.println("Error while parsing the command-line arguments: " + e.getMessage());
			new HelpFormatter().printHelp("jMonDB-core", options);
		}
		finally {
			if(emf != null)
				emf.close();
		}
	}

	private static Options createOptions() {
		Options options = new Options();
		// help
		options.addOption("?", "help", false, "show help");
		// MySQL connection options
		options.addOption(new Option("h", "host", true, "the iMonDB MySQL host"));
		options.addOption(new Option("p", "port", true, "the iMonDB MySQL port"));
		options.addOption(new Option("db", "database", true, "the iMonDB MySQL database"));
		options.addOption(new Option("u", "user", true, "the iMonDB MySQL user name"));
		options.addOption(new Option("pw", "password", true, "the iMonDB MySQL password"));
		// raw file options
		options.addOption(new Option("f", "file", true, "the raw file to store in the iMonDB"));
		options.addOption(new Option("pr", "project", true, "the project label to which the raw file belongs"));

		return options;
	}
}
