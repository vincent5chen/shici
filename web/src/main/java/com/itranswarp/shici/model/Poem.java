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
public class Poem extends BaseEntity {

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
	public int form;

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
