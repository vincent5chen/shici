package com.itranswarp.shici.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.itranswarp.warpdb.entity.BaseEntity;

/**
 * StaticPage object.
 * 
 * @author michael
 */
@Entity
@Table(indexes = @Index(unique = true, name = "UNI_StaticPage_alias", columnList = "alias"))
public class StaticPage extends BaseEntity {

	@Column(length = ENUM, nullable = false)
	public String alias;

	@Column(length = VARCHAR_100, nullable = false)
	public String name;

	@Column(columnDefinition = COLUMN_TEXT, nullable = false)
	public String content;

	@Override
	public int hashCode() {
		return Objects.hash(id, alias, name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof StaticPage) {
			StaticPage s = (StaticPage) o;
			return Objects.equals(this.id, s.id) && Objects.equals(this.alias, s.alias)
					&& Objects.equals(this.name, s.name);
		}
		return false;
	}

	@Override
	public String toString() {
		return "{StaticPage: " + name + ", alias=" + alias + "}";
	}
}
