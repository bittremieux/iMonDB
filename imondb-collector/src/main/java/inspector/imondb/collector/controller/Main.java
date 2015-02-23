package inspector.imondb.collector.controller;

/*
 * #%L
 * iMonDB Collector
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

import com.pagosoft.plaf.PlafOptions;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
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
                // log level
                if(cmd.hasOption("l")) {
                    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                    LoggerConfig loggerConfig = ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
                    switch(cmd.getOptionValue("l")) {
                        case "0":
                            loggerConfig.setLevel(Level.OFF);
                            break;
                        case "1":
                            loggerConfig.setLevel(Level.ERROR);
                            break;
                        case "2":
                            loggerConfig.setLevel(Level.WARN);
                            break;
                        case "3":
                        default:
                            loggerConfig.setLevel(Level.INFO);
                            break;
                        case "4":
                            loggerConfig.setLevel(Level.DEBUG);
                            break;
                        case "5":
                            loggerConfig.setLevel(Level.TRACE);
                            break;
                    }
                    ctx.updateLoggers();
                }

                // run
                CollectorController collectorController = new CollectorController("config.yaml");
                if(cmd.hasOption("c")) {
                    // run CLI
                    collectorController.startCliView();
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
        options.addOption(new Option("l", "log-level", true, "the log granularity level (0-5). 0: no logging, 5: extremely detailed logging, default: 3"));

		return options;
	}

    private static void setLookAndFeel() {
        PlafOptions.setAsLookAndFeel();
        PlafOptions.updateAllUIs();
    }
}
