package inspector.jmondb;

import inspector.jmondb.collect.Collector;
import inspector.jmondb.schedule.IMonDBScheduler;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
				new HelpFormatter().printHelp("jMonDB-collector", options);
			else {
				// immediate execution
				if(cmd.hasOption("rn"))
					new Collector();
				else if(cmd.hasOption("rd") || cmd.hasOption("rw")) {
					// scheduled execution
					int hour = 0;
					int minute = 0;
					if(cmd.hasOption("h"))
						hour = Integer.parseInt(cmd.getOptionValue("h"));
					else {
						logger.error("No hour provided");
						System.err.println("No hour provided");
						new HelpFormatter().printHelp("jMonDB-collector", options);
					}
					if(cmd.hasOption("m"))
						minute = Integer.parseInt(cmd.getOptionValue("m"));
					else {
						logger.error("No minute provided");
						System.err.println("No minute provided");
						new HelpFormatter().printHelp("jMonDB-collector", options);
					}

					if(cmd.hasOption("rd")) {
						logger.info("Start daily scheduling at {}:{}", String.format("%02d", hour), String.format("%02d", minute));
						new IMonDBScheduler().startDaily(hour, minute);
					}
					else if(cmd.hasOption("rw")) {
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
							logger.error("No day provided");
							System.err.println("No day provided");
							new HelpFormatter().printHelp("jMonDB-collector", options);
						}
					}
				}
				else {
					logger.error("No schedule information provided");
					System.err.println("No schedule information provided");
					new HelpFormatter().printHelp("jMonDB-collector", options);
				}
			}

		} catch (ParseException e) {
			logger.error("Error while parsing the command-line arguments: {}", e.getMessage());
			System.err.println("Error while parsing the command-line arguments: " + e.getMessage());
			new HelpFormatter().printHelp("jMonDB-collector", options);
		} catch(SchedulerException e) {
			logger.error("Error while executing the scheduler: {}", e.getMessage());
			System.err.println("Error while executing the scheduler: " + e.getMessage());
		}
	}

	private static Options createOptions() {
		Options options = new Options();
		// help
		options.addOption("?", "help", false, "show help");
		// scheduler options
		OptionGroup executionTime = new OptionGroup();
		executionTime.addOption(new Option("rn", "run-now", false, "run the jMonDB collector scheduler immediately"));
		executionTime.addOption(new Option("rd", "run-daily", false, "run the jMonDB collector scheduler daily"));
		executionTime.addOption(new Option("rw", "run-weekly", false, "run the jMonDB collector scheduler weekly"));
		options.addOptionGroup(executionTime);
		options.addOption("d", "day", true, "the day of the week that the scheduler runs (Sunday: 1, Monday: 2, ...)");
		options.addOption("h", "hour", true, "the hour of the day that the scheduler runs (0-23)");
		options.addOption("m", "minute", true, "the minute that the scheduler runs (0-59)");

		return options;
	}
}
