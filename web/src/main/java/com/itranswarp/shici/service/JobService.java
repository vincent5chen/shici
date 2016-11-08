package com.itranswarp.shici.service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itranswarp.shici.context.UserContext;
import com.itranswarp.shici.exception.APIEntityNotFoundException;
import com.itranswarp.shici.exception.InvalidJobStatusException;
import com.itranswarp.shici.model.Job;
import com.itranswarp.shici.model.JobHistory;
import com.itranswarp.shici.util.IdUtils;
import com.itranswarp.shici.util.JsonUtil;
import com.itranswarp.shici.util.MapUtil; 
import com.itranswarp.warpdb.PagedResults; 

/**
 * Services for async jobs.
 * 
 * @author michael
 */
@RestController
public class JobService extends AbstractService {

	@RequestMapping(value = "/api/jobs", method = RequestMethod.GET)
	public PagedResults<Job> getJobs(@RequestParam(value = "status", defaultValue = "") String status,
			@RequestParam(value = "page", defaultValue = "1") int page) {
		if (status.isEmpty()) {
			// get all jobs:
			return warpdb.from(Job.class).orderBy("createdAt desc").list(page);
		} else {
			return warpdb.from(Job.class).where("status=?", status).orderBy("createdAt desc").list(page);
		}
	}

	@RequestMapping(value = "/api/jobs/{id}/histories", method = RequestMethod.GET)
	public Map<String, List<JobHistory>> restGetJobHistories(@PathVariable(value = "id") String jobId) {
		return MapUtil.createMap("results", getJobHistories(jobId));
	}

	public List<JobHistory> getJobHistories(String jobId) {
		return warpdb.from(JobHistory.class).where("jobId=?", jobId).orderBy("createdAt desc").list();
	}

	/**
	 * Create a job.
	 */
	public Job createJob(String type, String refId, String name, Object data, TimeUnit unit, long estimateTime) {
		return createJob(type, refId, name, data, unit, estimateTime, Job.DEFAULT_RETRIES);
	}

	/**
	 * Create a job.
	 */
	public Job createJob(String type, String refId, String name, Object data, TimeUnit unit, long estimateTime,
			int maxRetries) {
		Job job = new Job();
		job.id = IdUtils.next();
		job.userId = UserContext.getRequiredCurrentUser().id;
		job.refId = refId;
		job.name = name;
		job.type = type;
		job.estimateTime = unit.toSeconds(estimateTime);
		job.retry = maxRetries;
		job.status = Job.Status.PENDING;
		job.data = JsonUtil.toJson(data);
		warpdb.save(job, JobHistory.info(job.id, "Job created."));
		return job;
	}

	/**
	 * Fetch a job and set job status to EXECUTING, timeoutAt, startAt, etc.
	 * 
	 * @return Job object or null if no job available.
	 */
	@Transactional
	public Job fetchJob(String type) {
		// query:
		List<Job> list = warpdb.list("select * from Job where type=? and status=? order by updatedAt desc limit 1",
				type, Job.Status.PENDING);
		if (list.isEmpty()) {
			return null;
		}
		String jobId = list.get(0).id;
		// lock for update:
		List<Job> jobs = warpdb.list("select * from Job where id=? for update", jobId);
		Job job = jobs.get(0);
		if (!job.status.equals(Job.Status.PENDING)) {
			return null;
		}
		// update the job and return:
		job.status = Job.Status.EXECUTING;
		job.startAt = System.currentTimeMillis();
		job.timeoutAt = job.startAt + job.estimateTime * 1000L;
		warpdb.updateProperties(job, "status", "startAt", "timeoutAt");
		warpdb.save(JobHistory.info(job.id, "Job started."));
		return job;
	}

	@Transactional
	public void setJobComplete(String jobId) {
		List<Job> jobs = warpdb.list("select * from Job where id=? for update", jobId);
		if (jobs.isEmpty()) {
			throw new APIEntityNotFoundException(Job.class);
		}
		Job job = jobs.get(0);
		if (!Job.Status.EXECUTING.equals(job.status)) {
			throw new InvalidJobStatusException("Job is not executing: " + jobId);
		}
		job.status = Job.Status.DONE;
		job.completeAt = System.currentTimeMillis();
		job.timeoutAt = 0;
		job.lastError = "";
		warpdb.updateProperties(job, "status", "completeAt", "timeoutAt", "lastError");
		warpdb.save(JobHistory.info(job.id, "Job completed."));
	}

	/**
	 * Set job as failed.
	 * 
	 * @param jobId
	 *            The job id.
	 * @param errorMessage
	 *            The error message.
	 * @param t
	 *            The exception.
	 * @return true if failed, false if can retry.
	 */
	@Transactional
	public boolean setJobFailed(String jobId, String errorMessage, Throwable t) {
		List<Job> jobs = warpdb.list("select * from Job where id=? for update", jobId);
		if (jobs.isEmpty()) {
			throw new APIEntityNotFoundException(Job.class);
		}
		Job job = jobs.get(0);
		if (!Job.Status.EXECUTING.equals(job.status)) {
			throw new InvalidJobStatusException("Job is not executing: " + jobId);
		}
		if (job.retried < job.retry) {
			// retry job:
			job.retried++;
			job.status = Job.Status.PENDING;
			job.completeAt = System.currentTimeMillis();
			job.timeoutAt = 0;
			job.lastError = errorMessage;
			warpdb.updateProperties(job, "retried", "status", "completeAt", "timeoutAt", "lastError");
			warpdb.save(JobHistory.error(job.id, errorMessage + "\n" + toStackTrace(t)));
			warpdb.save(JobHistory.info(job.id, "Will retry job later."));
			return false;
		} else {
			// job failed:
			job.status = Job.Status.FAILED;
			job.completeAt = System.currentTimeMillis();
			job.timeoutAt = 0;
			job.lastError = errorMessage;
			warpdb.updateProperties(job, "status", "completeAt", "timeoutAt", "lastError");
			warpdb.save(JobHistory.error(job.id, errorMessage + "\n" + toStackTrace(t)));
			return true;
		}
	}

	@Transactional
	public void setJobTimeout(String jobId) {
		List<Job> jobs = warpdb.list("select * from Job where id=? for update", jobId);
		if (jobs.isEmpty()) {
			throw new APIEntityNotFoundException(Job.class);
		}
		Job job = jobs.get(0);
		if (!Job.Status.EXECUTING.equals(job.status)) {
			// ignore:
			return;
		}
		if (job.retried < job.retry) {
			job.retried++;
			job.status = Job.Status.PENDING;
			job.lastError = "Job timeout and wait for retry.";
		} else {
			job.status = Job.Status.FAILED;
			job.lastError = "Job timeout.";
		}
		job.timeoutAt = System.currentTimeMillis();
		warpdb.updateProperties(job, "retried", "status", "timeoutAt", "lastError");
		warpdb.save(JobHistory.error(job.id, job.lastError));
	}

	@Transactional
	public void restartJob(String jobId) {
		List<Job> jobs = warpdb.list("select * from Job where id=? for update", jobId);
		if (jobs.isEmpty()) {
			throw new APIEntityNotFoundException(Job.class);
		}
		Job job = jobs.get(0);
		if (!Job.Status.FAILED.equals(job.status)) {
			throw new InvalidJobStatusException("Job is not failed: " + jobId);
		}
		job.status = Job.Status.PENDING;
		job.startAt = System.currentTimeMillis();
		job.retried = 0;
		warpdb.updateProperties(job, "status", "startAt", "retried");
		warpdb.save(JobHistory.info(job.id, "Job is restarted."));
	}

	/**
	 * Find job that are timeout.
	 * 
	 * @return The timeout job, or null if no timeout job.
	 */
	@Transactional
	public Job findTimeoutJob(long timeoutAt) {
		List<Job> jobs = warpdb.list("select * from Job where timeoutAt>? and status=? order by timeoutAt limit 1",
				timeoutAt, Job.Status.EXECUTING);
		if (jobs.isEmpty()) {
			return null;
		}
		return jobs.get(0);
	}

	String toStackTrace(Throwable t) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
			try (PrintStream ps = new PrintStream(output, false, "utf-8")) {
				t.printStackTrace(ps);
			}
			return output.toString("utf-8");
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
