package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Read only Job history object.
 * 
 * @author michael
 */
@Entity
@Table(indexes = @Index(name = "IDX_JobHistory_jobId", columnList = "jobId"))
public class JobHistory extends BaseEntity {

	public static interface Status {
		static final String INFO = "Info";
		static final String ERROR = "Error";
	}

	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String jobId;

	@Column(length = ENUM, nullable = false, updatable = false)
	public String status;

	@Column(columnDefinition = COLUMN_TEXT, nullable = false, updatable = false)
	public String message = "";

	public static JobHistory info(String jobId, String message) {
		JobHistory jh = new JobHistory();
		jh.jobId = jobId;
		jh.message = message;
		jh.status = Status.INFO;
		return jh;
	}

	public static JobHistory error(String jobId, String message) {
		JobHistory jh = new JobHistory();
		jh.jobId = jobId;
		jh.message = message;
		jh.status = Status.ERROR;
		return jh;
	}
}
