package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.itranswarp.warpdb.entity.BaseEntity;

@Entity
@Table
public class Hanz extends BaseEntity {

	@Column(columnDefinition = "char(1)", nullable = false)
	public String s;

	@Column(columnDefinition = "char(1)", nullable = false)
	public String t;

	@Override
	public String toString() {
		return "{Hanz: " + s + " -> " + t + "}";
	}
}
