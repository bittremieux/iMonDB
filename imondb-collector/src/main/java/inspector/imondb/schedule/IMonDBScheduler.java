package inspector.imondb.schedule;

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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.CronScheduleBuilder.*;

/**
 * A scheduler to periodically extract the instrument monitoring data from raw files and store the data in the iMonDB.
 *
 * The scheduler can be run daily or weekly.
 */
public class IMonDBScheduler {

	protected static final Logger logger = LogManager.getLogger(IMonDBScheduler.class);

	private Scheduler scheduler;

	/**
	 * Schedules the storage of raw file data in the iMonDB.
	 */
	public IMonDBScheduler() {
		try {
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			scheduler = schedulerFactory.getScheduler();
			scheduler.start();
		} catch(SchedulerException e) {
			logger.error("Exception while running the job scheduler: {}", e.getMessage());
		}
	}

	private JobDetail createCollectorJob() {
		return newJob(CollectorJob.class).withIdentity("imondb-collector").build();
	}

	/**
	 * Executes the scheduler on a daily basis at the specified time.
	 *
	 * @param hour  The hour of day that the scheduler runs
	 * @param minute  The minute that the scheduler runs
	 * @throws SchedulerException
	 */
	public void startDaily(int hour, int minute) throws SchedulerException {
		JobDetail job = createCollectorJob();

		// check the validity of the arguments
		DateBuilder.validateHour(hour);
		DateBuilder.validateMinute(minute);

		Trigger trigger = newTrigger().withIdentity("trigger-daily")
				.withSchedule(dailyAtHourAndMinute(hour, minute)).forJob(job).build();

		scheduler.scheduleJob(job, trigger);
	}

	/**
	 * Executes the scheduler on a weekly basis on the specified day at the specified time.
	 *
	 * @param day  The day of the week that the scheduler runs.
	 *             Use the {@link DateBuilder} class to easily set the correct day.
	 * @param hour  The hour of day that the scheduler runs
	 * @param minute  The minute that the scheduler runs
	 * @throws SchedulerException
	 */
	public void startWeekly(int day, int hour, int minute) throws SchedulerException {
		JobDetail job = createCollectorJob();

		// check the validity of the arguments
		DateBuilder.validateDayOfWeek(day);
		DateBuilder.validateHour(hour);
		DateBuilder.validateMinute(minute);

		Trigger trigger = newTrigger().withIdentity("trigger-weekly")
				.withSchedule(weeklyOnDayAndHourAndMinute(day, hour, minute)).forJob(job).build();

		scheduler.scheduleJob(job, trigger);
	}

	/**
	 * Shuts down the scheduler after all running jobs have completed
	 *
	 * @throws SchedulerException
	 */
	public void shutdown() throws SchedulerException {
		scheduler.shutdown(true);
	}
}
