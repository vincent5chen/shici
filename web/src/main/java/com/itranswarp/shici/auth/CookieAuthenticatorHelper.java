package com.itranswarp.shici.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.model.OAuth;
import com.itranswarp.shici.util.Base64Util;
import com.itranswarp.shici.util.HashUtil;
import com.itranswarp.warpdb.Database;

@Component
public class CookieAuthenticatorHelper {

	static final String COOKIE_SALT = "RandomSalt";

	final Log log = LogFactory.getLog(getClass());

	String sessionCookieName = "scsession";
	int sessionCookieExpires = 7200;
	int sessionCookieRefreshTime = 300;

	@Autowired
	Database database;

	/**
	 * Encode value as type : auth_id : expires : sha1(type : auth_id : expires
	 * : userSalt : password : salt)
	 * 
	 * @param auth
	 * @return
	 */
	public String encode(CookieAuth auth, String userSalt) {
		String expiresTime = String.valueOf(sessionCookieExpires * 1000L + System.currentTimeMillis());
		String sha1 = HashUtil.sha1(new StringBuilder(150).append(auth.authType).append(':').append(auth.authId)
				.append(':').append(expiresTime).append(':').append(auth.authPassword).append(':').append(COOKIE_SALT)
				.toString());
		return new StringBuilder(150).append(auth.authType).append(':').append(auth.authId).append(':')
				.append(expiresTime).append(':').append(sha1).append(':').append(userSalt).toString();
	}

	public CookieAuthHolder decode(String str) {
		String[] ss = str.split("\\:", 5);
		if (ss.length != 5) {
			return null;
		}
		String type = ss[0];
		String theId = ss[1];
		String expiresTime = ss[2];
		String sha1 = ss[3];
		String userSalt = ss[4];
		long expires = 0;
		if (sha1.length() != 40) {
			log.info("Invalid sha1: " + str);
			return null;
		}
		try {
			expires = Long.parseLong(expiresTime);
		} catch (NumberFormatException e) {
			log.info("Invalid expires: " + str);
			return null;
		}
		if (expires < System.currentTimeMillis()) {
			log.info("token expires: " + str);
			return null;
		}
		CookieAuth cauth = null;
		// oauth signin:
		OAuth oa = database.fetch(OAuth.class, theId);
		if (oa != null && type.equals(oa.provider)) {
			cauth = new CookieAuth(oa.userId, oa.provider, oa.id, oa.accessToken);
		}
		if (cauth == null) {
			log.info("Auth not found by id: " + str);
			return null;
		}
		String expectedSha1 = HashUtil.sha1(new StringBuilder(150).append(cauth.authType).append(':')
				.append(cauth.authId).append(':').append(expiresTime).append(':').append(cauth.authPassword).append(':')
				.append(COOKIE_SALT).toString());
		if (!sha1.equals(expectedSha1)) {
			log.info("Sha1 failed: " + str);
			return null;
		}
		return new CookieAuthHolder(cauth, expires, userSalt);
	}

	/**
	 * Get session cookie value, or null if not found.
	 */
	String getSessionCookieValue(HttpServletRequest request, String sessionCookieName) {
		Cookie[] cs = request.getCookies();
		if (cs == null) {
			return null;
		}
		for (Cookie c : cs) {
			if (sessionCookieName.equals(c.getName())) {
				try {
					return Base64Util.urlDecodeToString(c.getValue());
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		}
		return null;
	}

	public void setSessionCookie(HttpServletResponse response, String sessionCookieValue) {
		String encodedValue = Base64Util.urlEncodeToString(sessionCookieValue);
		_setCookie(response, encodedValue, this.sessionCookieExpires);
	}

	public void deleteSessionCookie(HttpServletResponse response) {
		_setCookie(response, "-deleted-", 0);
	}

	void _setCookie(HttpServletResponse response, String value, int expiry) {
		Cookie cookie = new Cookie(sessionCookieName, value);
		cookie.setMaxAge(expiry);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}

	boolean needRefresh(long expiresAt) {
		return (expiresAt - System.currentTimeMillis()) > this.sessionCookieRefreshTime * 1000L;
	}
}
