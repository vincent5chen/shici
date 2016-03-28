package com.itranswarp.shici.auth;

public class CookieAuthHolder {

	public final long expires;
	public final CookieAuth cookieAuth;
	public final String userSalt;

	public CookieAuthHolder(CookieAuth cookieAuth, long expires, String userSalt) {
		this.cookieAuth = cookieAuth;
		this.expires = expires;
		this.userSalt = userSalt;
	}

}
