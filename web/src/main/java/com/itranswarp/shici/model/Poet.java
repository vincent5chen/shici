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

	@Column(nullable = false)
	public int poemCount;

	@Column(length = VARCHAR_100, nullable = false)
	public String birth;

	@Column(length = VARCHAR_100, nullable = false)
	public String death;

	@Column(length = VARCHAR_100, nullable = false)
	public String name;

	@Column(length = VARCHAR_100, nullable = false)
	public String nameCht;

	@Column(length = VARCHAR_1000, nullable = false)
	public String description;

	@Column(length = VARCHAR_1000, nullable = false)
	public String descriptionCht;

	@Override
	public String toString() {
		return "{Poet: name=" + name + "}";
	}

}
