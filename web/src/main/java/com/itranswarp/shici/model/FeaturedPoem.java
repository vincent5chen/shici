package com.itranswarp.shici.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.itranswarp.warpdb.entity.BaseEntity;

/**
 * Featured poems.
 * 
 * @author michael
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UNI_PubDate", columnNames = "pubDate"),
		@UniqueConstraint(name = "UNI_F_PoemId", columnNames = "poemId") })
public class FeaturedPoem extends BaseEntity {

	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String poemId;

	@Column(columnDefinition = "date", nullable = false, updatable = false)
	public LocalDate pubDate;

	@Override
	public String toString() {
		return "{FeaturedPoem: poemId=" + poemId + ", pubDate=" + pubDate + "}";
	}

}
