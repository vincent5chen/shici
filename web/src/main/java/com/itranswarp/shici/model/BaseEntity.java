package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.itranswarp.shici.context.UserContext;
import com.itranswarp.shici.util.IdUtils;

@MappedSuperclass
public abstract class BaseEntity {

	public static final int ID_LENGTH = 16;
	public static final int ENUM = 50;
	public static final int VARCHAR_50 = 50;
	public static final int VARCHAR_100 = 100;
	public static final int VARCHAR_500 = 500;
	public static final int VARCHAR_1000 = 1000;
	public static final String COLUMN_TEXT = "mediumtext";

	@Id
	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String id;

	@Column(nullable = false, updatable = false)
	public long createdAt;

	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String createdBy;

	@Column(nullable = false)
	public long updatedAt;

	@Column(length = ID_LENGTH, nullable = false)
	public String updatedBy;

	@Column(nullable = false)
	public long version;

	@PrePersist
	public void prePersist() {
		if (this.id == null) {
			this.id = IdUtils.next();
		}
		this.createdAt = this.updatedAt = System.currentTimeMillis();
		this.createdBy = this.updatedBy = UserContext.getRequiredCurrentUser().id;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = System.currentTimeMillis();
		this.updatedBy = UserContext.getRequiredCurrentUser().id;
		this.version++;
	}
}
