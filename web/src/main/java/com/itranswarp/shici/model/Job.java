package com.itranswarp.shici.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.itranswarp.warpdb.entity.BaseEntity;

/**
 * Job object.
 * 
 * @author michael
 */
@Entity
public class Job extends BaseEntity {

	public static final int DEFAULT_RETRIES = 3;

	public interface Status {
		final static String PENDING = "Pending";
		final static String EXECUTING = "Executing";
		final static String FAILED = "Failed";
		final static String DONE = "Done";
	}

	public interface Type {
		final static String INDEX = "Index";
		final static String UNINDEX = "Unindex";
	}

	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String userId;

	/**
	 * Job status.
	 */
	@Column(length = ENUM, nullable = false)
	public String status;

	@Column(length = VARCHAR_100, nullable = false, updatable = false)
	public String name;

	@Column(length = ENUM, nullable = false, updatable = false)
	public String type = "";

	/**
	 * Reference to the id. Can cannot be empty.
	 */
	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String refId = "";

	/**
	 * Estimate how long this job will be executed in seconds.
	 */
	@Column(nullable = false, updatable = false)
	public long estimateTime;

	@Column(nullable = false, updatable = false)
	public long retry = DEFAULT_RETRIES;

	@Column(nullable = false, updatable = true)
	public long retried;

	@Column(nullable = false, updatable = true)
	public long startAt;

	@Column(nullable = false, updatable = true)
	public long completeAt;

	@Column(nullable = false, updatable = true)
	public long timeoutAt;

	@Column(length = VARCHAR_1000, nullable = false, updatable = true)
	public String lastError = "";

	@Column(columnDefinition = COLUMN_TEXT, nullable = false, updatable = false)
	public String data;

	@Override
	public int hashCode() {
		return Objects.hash(id, userId, refId, type, name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof Job) {
			Job j = (Job) o;
			return Objects.equals(this.id, j.id) && Objects.equals(this.userId, j.userId)
					&& Objects.equals(this.refId, j.refId) && Objects.equals(this.type, j.type)
					&& Objects.equals(this.name, j.name);
		}
		return false;
	}

	@Override
	public String toString() {
		return "{Job: status=" + status + ", name=" + name + "}";
	}
}
