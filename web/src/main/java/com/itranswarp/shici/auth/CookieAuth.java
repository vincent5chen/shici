package com.itranswarp.shici.auth;

public class CookieAuth {

	public final String userId;

	public final String authType;
	public final String authId;
	public final String authPassword;

	public CookieAuth(String userId, String authType, String authId, String authPassword) {
		this.userId = userId;
		this.authType = authType;
		this.authId = authId;
		this.authPassword = authPassword;
	}

}
