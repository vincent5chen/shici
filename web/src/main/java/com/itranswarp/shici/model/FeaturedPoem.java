package com.itranswarp.shici.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.itranswarp.shici.json.LocalDateDeserializer;
import com.itranswarp.shici.json.LocalDateSerializer;

/**
 * Featured poems.
 * 
 * @author michael
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UNI_F_PubDate", columnNames = "pubDate"),
		@UniqueConstraint(name = "UNI_F_PoemId", columnNames = "poemId") })
public class FeaturedPoem extends BaseEntity {

	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String poemId;

	@Convert(converter = LocalDateConverter.class)
	@Column(columnDefinition = "date", nullable = false, updatable = false)
	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	public LocalDate pubDate;

	@Override
	public String toString() {
		return "{FeaturedPoem: poemId=" + poemId + ", pubDate=" + pubDate + "}";
	}

}
