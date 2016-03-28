package com.itranswarp.shici.auth;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.model.User;
import com.itranswarp.shici.service.UserService;

/**
 * Authenticate by cookie.
 * 
 * @author michael
 */
@Component
@Priority(30)
public class CookieAuthenticator implements Authenticator {

	final Log log = LogFactory.getLog(getClass());

	long refreshTime = 300 * 1000L;

	@Autowired
	UserService userService;

	@Autowired
	CookieAuthenticatorHelper cookieHelper;

	@Override
	public User authenticate(HttpServletRequest request, HttpServletResponse response) {
		String cookieValue = cookieHelper.getSessionCookieValue(request, cookieHelper.sessionCookieName);
		if (cookieValue == null) {
			return null;
		}
		CookieAuthHolder holder = cookieHelper.decode(cookieValue);
		if (holder == null) {
			cookieHelper.deleteSessionCookie(response);
			return null;
		}
		User user = userService.fetchUser(holder.cookieAuth.userId);
		if (user == null) {
			cookieHelper.deleteSessionCookie(response);
			return null;
		}
		if (holder.expires - System.currentTimeMillis() < refreshTime) {
			cookieHelper.setSessionCookie(response, cookieHelper.encode(holder.cookieAuth, holder.userSalt));
		}
		return user;
	}

}
