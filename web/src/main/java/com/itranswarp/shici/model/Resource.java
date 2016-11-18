package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Category that contains a list of courses.
 * 
 * @author michael
 */
@Entity
public class Resource extends BaseEntity {

	@Column(length = VARCHAR_100, nullable = false, updatable = false)
	public String refType;

	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String refId;

	@Column(nullable = false)
	public boolean deleted;

	@Column(nullable = false)
	public int size;

	@Column(length = VARCHAR_100, nullable = false)
	public String name;

	@Column(length = VARCHAR_100, nullable = false)
	public String meta;

	@Column(length = VARCHAR_100, nullable = false)
	public String mime;

	@Column(columnDefinition = COLUMN_TEXT, nullable = false)
	public String data;

	@Override
	public String toString() {
		return "{Resource: name=" + name + ", size=" + size + "}";
	}

}
