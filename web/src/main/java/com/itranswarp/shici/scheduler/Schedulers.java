package com.itranswarp.shici.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.service.JobService;
import com.itranswarp.wxapi.WeixinClient;

@Component
public class Schedulers {

	// 1 minute in milliseconds:
	static final long ONE_MINUTE = 60000L;

	@Autowired
	JobService jobService;

	@Autowired
	IndexTask indexTask;

	@Autowired
	UnindexTask unindexTask;

	@Autowired
	WeixinClient weixinClient;

	@Scheduled(initialDelay = 1000, fixedRate = WeixinClient.REFRESH_TOKEN_IN_MILLIS)
	public void refreshAccessToken() {
		weixinClient.refreshAccessToken();
	}

	@Scheduled(initialDelay = ONE_MINUTE * 10, fixedRate = ONE_MINUTE * 10)
	public void scanTimeoutJobs() {
		jobService.findTimeoutJob(System.currentTimeMillis());
	}

	@Scheduled(initialDelay = ONE_MINUTE * 5, fixedRate = ONE_MINUTE * 10)
	public void executeIndexTask() {
		indexTask.execute();
	}

	@Scheduled(initialDelay = ONE_MINUTE * 5, fixedRate = ONE_MINUTE * 50)
	public void executeUnindexTask() {
		unindexTask.execute();
	}
}
