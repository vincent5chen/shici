package com.itranswarp.shici.scheduler;

import com.itranswarp.shici.model.Job;

public class UnindexTask extends AbstractTask {

	@Override
	protected String getJobType() {
		return Job.Type.UNINDEX;
	}

	@Override
	protected void run(String refId, String data, long timeoutAt) throws Exception {

	}

}
