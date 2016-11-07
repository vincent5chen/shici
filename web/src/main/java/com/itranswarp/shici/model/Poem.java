package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Poem object.
 * 
 * @author michael
 */
@Entity
@Table
public class Poem extends BaseEntity {

	public static interface Form {
		static final long UNKNOWN = 0;
		static final long WU_JUE = 54;
		static final long WU_LV = 58;
		static final long QI_JUE = 74;
		static final long QI_LV = 78;
		static final long CI = 9;
		static final long QU = 8;
		static final long FU = 15;

		static final long[] ALL = { UNKNOWN, WU_JUE, WU_LV, QI_JUE, QI_LV, CI, QU, FU };
	}

	@Column(length = ID_LENGTH, nullable = false)
	public String dynastyId;

	@Column(length = ID_LENGTH, nullable = false)
	public String imageId;

	@Column(length = ID_LENGTH, nullable = false)
	public String poetId;

	@Column(length = VARCHAR_100, nullable = false)
	public String poetName;

	@Column(length = VARCHAR_100, nullable = false)
	public String poetNameCht;

	@Column(nullable = false)
	public long form;

	@Column(length = VARCHAR_100, nullable = false)
	public String tags;

	@Column(length = VARCHAR_100, nullable = false)
	public String name;

	@Column(length = VARCHAR_100, nullable = false)
	public String nameCht;

	@Column(columnDefinition = "text", nullable = false)
	public String content;

	@Column(columnDefinition = "text", nullable = false)
	public String contentCht;

	@Column(length = VARCHAR_1000, nullable = false)
	public String appreciation;

	@Column(length = VARCHAR_1000, nullable = false)
	public String appreciationCht;

	@Override
	public String toString() {
		return "{Poem: name=" + name + ", poet=" + poetName + "}";
	}
}
