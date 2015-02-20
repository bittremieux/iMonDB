package inspector.imondb.collector.controller;

import com.pagosoft.plaf.PlafOptions;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class Main {

	protected static final Logger LOGGER = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		// create commandline options
		Options options = createOptions();

		try {
            // parse arguments
            CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);

			if(cmd.hasOption("?")) {
                // show CLI help
                new HelpFormatter().printHelp("iMonDB-collector", options, true);
            } else {
                // logging verbosity
                if(cmd.hasOption("v") || cmd.hasOption("vv")) {
                    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                    Configuration config = ctx.getConfiguration();
                    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
                    if(cmd.hasOption("v")) {
                        loggerConfig.setLevel(Level.DEBUG);
                    } else if(cmd.hasOption("vv")) {
                        loggerConfig.setLevel(Level.TRACE);
                    }
                    ctx.updateLoggers();
                }

                // run
                CollectorController collectorController = new CollectorController("config.yaml");
                if(cmd.hasOption("c")) {
                    // run CLI
                } else {
                    // run GUI
                    setLookAndFeel();
                    collectorController.startGuiView();
                }
            }
		} catch(ParseException e) {
			LOGGER.fatal("Error while parsing the command-line arguments: {}", e.getMessage());
			new HelpFormatter().printHelp("iMonDB-collector", options, true);
		}
	}

	private static Options createOptions() {
		Options options = new Options();
        // run
        OptionGroup run = new OptionGroup();
        run.addOption(new Option("c", "cli", false, "run from the command-line interface"));
        run.addOption(new Option("g", "gui", false, "run from the graphical user interface (default)"));
        run.addOption(new Option("?", "help", false, "show help"));
        options.addOptionGroup(run);
		// logging verbosity
		OptionGroup logging = new OptionGroup();
		logging.addOption(new Option("v", "verbose", false, "verbose logging"));
		logging.addOption(new Option("vv", "very-verbose", false, "extremely verbose logging"));
		options.addOptionGroup(logging);

		return options;
	}

    private static void setLookAndFeel() {
        PlafOptions.setAsLookAndFeel();
        PlafOptions.updateAllUIs();
    }
}
