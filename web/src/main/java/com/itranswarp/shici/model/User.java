package com.itranswarp.shici.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.itranswarp.warpdb.entity.BaseEntity;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UK_User_email", columnNames = { "email" }))
public class User extends BaseEntity {

	public static interface Role {
		static final int ADMIN = 0;
		static final int EDITOR = 100;
		static final int USER = 10000;
	}

	public static interface Gender {
		static final String MALE = "male";
		static final String FEMALE = "female";
		static final String UNKNOWN = "unknown";

		static final Set<String> SET = Collections
				.unmodifiableSet(new HashSet<String>(Arrays.asList(MALE, FEMALE, UNKNOWN)));
	}

	public static final User SYSTEM;

	static {
		// create system user ID = "00000..."
		char[] cs = new char[ID_LENGTH];
		for (int i = 0; i < cs.length; i++) {
			cs[i] = '0';
		}
		final String ID = new String(cs);
		User sys = new User();
		sys.id = ID;
		sys.name = "Administrator";
		sys.email = "admin@shi-ci.com";
		sys.gender = User.Gender.FEMALE;
		sys.role = Role.ADMIN;
		sys.verified = true;
		sys.imageUrl = "/static/img/admin.png";
		sys.salt = "00000000-0000-0000-0000-000000000000";
		sys.createdBy = ID;
		sys.updatedBy = ID;
		SYSTEM = sys;
	}

	@Column(nullable = false, updatable = false)
	public int role;

	@Column(length = VARCHAR_100, nullable = false, updatable = false)
	public String name;

	@Column(length = VARCHAR_100, nullable = false, updatable = false)
	public String email;

	@Column(length = ENUM, nullable = false)
	public String gender;

	@Column(nullable = false)
	public boolean verified;

	@Column(length = VARCHAR_1000, nullable = false, updatable = false)
	public String imageUrl;

	@Column(nullable = false)
	public long lockedUntil;

	@Column(length = VARCHAR_100, nullable = false, updatable = false)
	public String salt;

}
