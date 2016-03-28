package com.itranswarp.shici.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.itranswarp.warpdb.entity.BaseEntity;

/**
 * Using OAuth2 authentication.
 * 
 * @author michael
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UNI_OAuth_prov_oid", columnNames = { "provider", "oauthId" }))
public class OAuth extends BaseEntity {

	@Column(length = ID_LENGTH, nullable = false, updatable = false)
	public String userId;

	@Column(length = VARCHAR_100, nullable = false, updatable = false)
	public String provider;

	@Column(length = 255, nullable = false)
	public String oauthId;

	@Column(length = VARCHAR_1000, nullable = false)
	public String accessToken;

	@Column(length = VARCHAR_1000, nullable = false)
	public String refreshToken;

	@Column(nullable = false)
	public long expiresAt;

	@Override
	public String toString() {
		return "{OAuth: userId=" + userId + ", provider=" + provider + ", oauthId=" + oauthId + "}";
	}
}
