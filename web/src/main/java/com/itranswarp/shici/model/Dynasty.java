package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.itranswarp.warpdb.entity.BaseEntity;

/**
 * Poem object.
 * 
 * @author michael
 */
@Entity
public class Dynasty extends BaseEntity {

	@Column(nullable = false)
	public long displayOrder;

	@Column(length = VARCHAR_100, nullable = false)
	public String name;

	@Column(length = VARCHAR_1000, nullable = false)
	public String description;

	@Override
	public String toString() {
		return "{Dynasty: name=" + name + "}";
	}
}
