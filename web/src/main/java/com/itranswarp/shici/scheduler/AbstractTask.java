package com.itranswarp.shici.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.itranswarp.shici.model.Job;
import com.itranswarp.shici.model.User;
import com.itranswarp.shici.service.JobService;
import com.itranswarp.warpdb.context.UserContext;

public abstract class AbstractTask {

	protected final Log log = LogFactory.getLog(getClass());

	@Autowired
	JobService jobService;

	public final void execute() {
		log.info("Start scheduled task: " + getClass().getSimpleName());
		try (UserContext<User> ctx = new UserContext<User>(User.SYSTEM)) {
			Job job = null;
			try {
				job = jobService.fetchJob(getJobType());
				if (job == null) {
					log.info("There is no pending job right now.");
				} else {
					log.info("Start execute job: " + job.id);
					run(job.refId, job.data, System.currentTimeMillis() + job.estimateTime * 1000);
					jobService.setJobComplete(job.id);
					log.info("End scheduled job: " + getClass().getSimpleName());
				}
			} catch (Exception e) {
				log.error("Scheduled job " + getClass().getSimpleName() + " error: " + e.getMessage(), e);
				jobService.setJobFailed(job.id, e.getMessage(), e);
			}
		}
	}

	protected abstract String getJobType();

	protected abstract void run(String refId, String data, long timeoutAt) throws Exception;
}
