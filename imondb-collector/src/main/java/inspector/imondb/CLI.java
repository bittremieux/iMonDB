package inspector.imondb;

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

import inspector.imondb.collect.Collector;
import inspector.imondb.schedule.IMonDBScheduler;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.quartz.DateBuilder;
import org.quartz.SchedulerException;

public class CLI {

	protected static final Logger logger = LogManager.getLogger(CLI.class);

	public static void main(String[] args) {
		// create commandline options
		Options options = createOptions();

		// parse arguments
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine cmd = parser.parse(options, args);

			// help
			if(cmd.hasOption("?"))
				new HelpFormatter().printHelp("iMonDB-collector", options, true);
			else {
				// logging verbosity
				if(cmd.hasOption("v") || cmd.hasOption("vv")) {
					LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
					Configuration config = ctx.getConfiguration();
					LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
					if(cmd.hasOption("v"))
						loggerConfig.setLevel(Level.INFO);
					else if(cmd.hasOption("vv"))
						loggerConfig.setLevel(Level.DEBUG);
					ctx.updateLoggers();
				}

				// immediate execution
				if(cmd.hasOption("rn")) {
					Collector collector = new Collector();
					collector.collect();
				}
				else if(cmd.hasOption("rd") || cmd.hasOption("rw")) {
					boolean error = false;

					// scheduled execution
					int hour = 0;
					int minute = 0;
					if(cmd.hasOption("h"))
						hour = Integer.parseInt(cmd.getOptionValue("h"));
					else {
						error = true;
						logger.fatal("No hour provided");
					}
					if(cmd.hasOption("m"))
						minute = Integer.parseInt(cmd.getOptionValue("m"));
					else {
						error = true;
						logger.fatal("No minute provided");
					}

					if(!error && cmd.hasOption("rd")) {
						logger.info("Start daily scheduling at {}:{}", String.format("%02d", hour), String.format("%02d", minute));
						new IMonDBScheduler().startDaily(hour, minute);
					}
					else if(!error && cmd.hasOption("rw")) {
						if(cmd.hasOption("d")) {
							int day = Integer.parseInt(cmd.getOptionValue("d"));
							String dayName;
							switch(day) {
								case DateBuilder.SUNDAY:
									dayName = "Sunday";
									break;
								case DateBuilder.MONDAY:
									dayName = "Monday";
									break;
								case DateBuilder.TUESDAY:
									dayName = "Tuesday";
									break;
								case DateBuilder.WEDNESDAY:
									dayName = "Wednesday";
									break;
								case DateBuilder.THURSDAY:
									dayName = "Thursday";
									break;
								case DateBuilder.FRIDAY:
									dayName = "Friday";
									break;
								case DateBuilder.SATURDAY:
									dayName = "Saturday";
									break;
								default:
									dayName = "Unknown";
									break;
							}
							logger.info("Start weekly scheduling on {} at {}:{}", dayName, String.format("%02d", hour), String.format("%02d", minute));
							new IMonDBScheduler().startWeekly(day, hour, minute);
						}
						else {
							error = true;
							logger.fatal("No day provided");
						}
					}

					if(error)
						new HelpFormatter().printHelp("iMonDB-collector", options, true);
				}
				else {
					logger.fatal("No schedule information provided");
					new HelpFormatter().printHelp("iMonDB-collector", options, true);
				}
			}

		} catch (ParseException e) {
			logger.fatal("Error while parsing the command-line arguments: {}", e.getMessage());
			new HelpFormatter().printHelp("iMonDB-collector", options, true);
		} catch(SchedulerException e) {
			logger.fatal("Error while executing the scheduler: {}", e.getMessage());
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
		// scheduler options
		OptionGroup executionTime = new OptionGroup();
		executionTime.addOption(new Option("rn", "run-now", false, "run the iMonDB collector scheduler immediately"));
		executionTime.addOption(new Option("rd", "run-daily", false, "run the iMonDB collector scheduler daily"));
		executionTime.addOption(new Option("rw", "run-weekly", false, "run the iMonDB collector scheduler weekly"));
		options.addOptionGroup(executionTime);
		options.addOption("d", "day", true, "the day of the week that the scheduler runs (Sunday: 1, Monday: 2, ...)");
		options.addOption("h", "hour", true, "the hour of the day that the scheduler runs (0-23)");
		options.addOption("m", "minute", true, "the minute that the scheduler runs (0-59)");

		return options;
	}
}
