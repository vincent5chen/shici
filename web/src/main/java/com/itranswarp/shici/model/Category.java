package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Category that contains poems.
 * 
 * @author michael
 */
@Entity
@Table
public class Category extends BaseEntity {

	@Column(length = VARCHAR_100, nullable = false)
	public String name;

	@Column(length = VARCHAR_100, nullable = false)
	public String nameCht;

	@Column(length = VARCHAR_1000, nullable = false)
	public String description;

	@Column(length = VARCHAR_1000, nullable = false)
	public String descriptionCht;

	@Column(nullable = false)
	public long displayOrder;

	@Override
	public String toString() {
		return "{Category: name=" + name + "}";
	}

}
