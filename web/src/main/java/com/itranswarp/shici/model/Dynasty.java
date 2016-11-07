package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;

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

	@Column(length = VARCHAR_100, nullable = false)
	public String nameCht;

	@Column(nullable = false)
	public int poetCount;

	@Column(nullable = false)
	public int poemCount;

	@Override
	public String toString() {
		return "{Dynasty: name=" + name + "}";
	}
}
