package com.itranswarp.shici.scheduler;

import org.springframework.stereotype.Component;

import com.itranswarp.shici.model.Job;

@Component
public class UnindexTask extends AbstractTask {

	@Override
	protected String getJobType() {
		return Job.Type.UNINDEX;
	}

	@Override
	protected void run(String refId, String data, long timeoutAt) throws Exception {

	}

}
