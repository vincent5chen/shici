package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Hanzi extends BaseEntity {

	@Column(columnDefinition = "char(1)", nullable = false)
	public String s;

	@Column(columnDefinition = "char(1)", nullable = false)
	public String t;

	@Override
	public String toString() {
		return "{Hanz: " + s + " -> " + t + "}";
	}
}
