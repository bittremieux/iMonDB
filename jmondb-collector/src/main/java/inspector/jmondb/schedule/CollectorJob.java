package inspector.jmondb.schedule;

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
		} catch(Exception e) {
			// catch all exceptions that might be thrown and rethrow it as a JobExecutionException
			throw new JobExecutionException(e);
		}

	}
}
