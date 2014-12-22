package inspector.jmondb.schedule;

/*
 * #%L
 * jMonDB Collector
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

import inspector.jmondb.collect.Collector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CollectorJob implements Job {

	protected static final Logger logger = LogManager.getLogger(CollectorJob.class);

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			logger.info("Executing the scheduled Collector job");

			Collector collector = new Collector();
			collector.collect();

		} catch(Exception e) {
			// catch all exceptions that might be thrown and rethrow it as a JobExecutionException
			throw new JobExecutionException(e);
		}

	}
}
