package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.itranswarp.warpdb.entity.BaseEntity;

/**
 * Poem object.
 * 
 * @author michael
 */
@Entity
@Table
public class Poet extends BaseEntity {

	@Column(length = ID_LENGTH, nullable = false)
	public String dynastyId;

	@Column(length = VARCHAR_100, nullable = false)
	public String name;

	@Column(length = VARCHAR_1000, nullable = false)
	public String description;

	@Override
	public String toString() {
		return "{Poet: name=" + name + "}";
	}
}
