package inspector.jmondb;

/*
 * #%L
 * jMonDB Core
 * %%
 * Copyright (C) 2014 InSPECtor
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

import inspector.jmondb.convert.thermo.ThermoRawFileExtractor;
import inspector.jmondb.io.IMonDBManagerFactory;
import inspector.jmondb.io.IMonDBWriter;
import inspector.jmondb.model.Run;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import javax.persistence.EntityManagerFactory;


public class CLI {

    private static final Logger LOGGER = LogManager.getLogger(CLI.class);

    private CLI() {

    }

    public static void main(String[] args) {

        EntityManagerFactory emf = null;

        // create commandline options
        Options options = createOptions();

        // parse arguments
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            // help
            if(cmd.hasOption("?")) {
                new HelpFormatter().printHelp("jMonDB-core", options, true);
            } else {
                boolean error = false;

                // logging verbosity
                if(cmd.hasOption("v") || cmd.hasOption("vv")) {
                    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                    Configuration config = ctx.getConfiguration();
                    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
                    if(cmd.hasOption("v")) {
                        loggerConfig.setLevel(Level.INFO);
                    } else if(cmd.hasOption("vv")) {
                        loggerConfig.setLevel(Level.DEBUG);
                    }
                    ctx.updateLoggers();
                }

                // database information
                String host = null;
                String port = null;
                String database = null;
                String user = null;
                String pass = null;
                if(cmd.hasOption("h")) {
                    host = cmd.getOptionValue("h");
                }
                if(cmd.hasOption("p")) {
                    port = cmd.getOptionValue("p");
                }
                if(cmd.hasOption("db")) {
                    database = cmd.getOptionValue("db");
                } else {
                    error = true;
                    LOGGER.fatal("No database provided");
                }
                if(cmd.hasOption("u")) {
                    user = cmd.getOptionValue("u");
                } else {
                    error = true;
                    LOGGER.fatal("No user name provided");
                }
                if(cmd.hasOption("pw")) {
                    pass = cmd.getOptionValue("pw");
                }

                // raw file information
                String rawFile = null;
                String instrumentName = null;
                if(cmd.hasOption("f")) {
                    rawFile = cmd.getOptionValue("f");
                } else {
                    error = true;
                    LOGGER.fatal("No raw file provided");
                }
                if(cmd.hasOption("i")) {
                    instrumentName = cmd.getOptionValue("i");
                } else {
                    error = true;
                    LOGGER.fatal("No instrument name provided");
                }

                if(!error) {
                    // create database connection
                    emf = IMonDBManagerFactory.createMySQLFactory(host, port, database, user, pass);
                    IMonDBWriter writer = new IMonDBWriter(emf);

                    // store raw file in the database
                    Run run = new ThermoRawFileExtractor().extractInstrumentData(rawFile, null, instrumentName);
                    writer.writeRun(run);
                } else {
                    new HelpFormatter().printHelp("jMonDB-core", options, true);
                }
            }
        } catch (ParseException e) {
            LOGGER.fatal("Error while parsing the command-line arguments: {}", e.getMessage());
            new HelpFormatter().printHelp("jMonDB-core", options, true);
        } finally {
            if(emf != null) {
                emf.close();
            }
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        // help
        options.addOption("?", "help", false, "show help");
        // logging verbosity
        OptionGroup logging = new OptionGroup();
        logging.addOption(new Option("v", "verbose", false, "verbose logging"));
        logging.addOption(new Option("vv", "very-verbose", false, "extremely verbose logging"));
        options.addOptionGroup(logging);
        // MySQL connection options
        options.addOption(new Option("h", "host", true, "the iMonDB MySQL host"));
        options.addOption(new Option("p", "port", true, "the iMonDB MySQL port"));
        options.addOption(new Option("db", "database", true, "the iMonDB MySQL database"));
        options.addOption(new Option("u", "user", true, "the iMonDB MySQL user name"));
        options.addOption(new Option("pw", "password", true, "the iMonDB MySQL password"));
        // raw file options
        options.addOption(new Option("f", "file", true, "the raw file to store in the iMonDB"));
        options.addOption(new Option("i", "instrument", true, "the name of the instrument on which the raw file was obtained (this instrument should be in the iMonDB already)"));

        return options;
    }
}
