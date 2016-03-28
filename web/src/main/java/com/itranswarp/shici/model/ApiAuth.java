package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.itranswarp.warpdb.entity.BaseEntity;

/**
 * ApiAuth stores a key-secret pair that associate to a user.
 * 
 * @author michael
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UK_ApiAuth_userId", columnNames = { "userId" }) )
public class ApiAuth extends BaseEntity {

	@Column(nullable = false)
	public boolean disabled;

	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String userId;

	@Column(length = VARCHAR_100, nullable = false)
	public String apiSecret;

	@Override
	public String toString() {
		return "{ApiAuth: userId=" + userId + ", apiSecret=******}";
	}
}
