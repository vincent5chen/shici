package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.itranswarp.warpdb.entity.BaseEntity;

/**
 * Many-to-many relationship between Category and Poem.
 * 
 * @author michael
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UNI_CategoryPoem", columnNames = { "categoryId",
		"poemId" }), indexes = @Index(name = "IDX_CategoryId", columnList = "categoryId"))
public class CategoryPoem extends BaseEntity implements ChineseSupport {

	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String categoryId;

	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String poemId;

	@Column(nullable = false)
	public long displayOrder;

	@Column(length = VARCHAR_100, nullable = false)
	public String sectionName;

	@Column(length = VARCHAR_100, nullable = false)
	public String sectionNameCht;

	@Override
	public void updateChinese() {
		this.sectionNameCht = this.sectionName;

	}

	@Override
	public String toString() {
		return "{CategoryPoem: categoryId=" + categoryId + ", poemId=" + poemId + ", displayOrder=" + displayOrder
				+ "}";
	}

}
