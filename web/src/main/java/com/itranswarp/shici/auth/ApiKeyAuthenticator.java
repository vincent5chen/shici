package com.itranswarp.shici.auth;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.exception.APIAuthenticationException;
import com.itranswarp.shici.model.ApiAuth;
import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.Database;

/**
 * Authenticate by header X-API-Key and X-API-Secret.
 * 
 * @author michael
 */
@Component
@Priority(10)
public class ApiKeyAuthenticator implements Authenticator {

	final Log log = LogFactory.getLog(getClass());

	static final String HEADER_API_KEY = "X-API-Key";
	static final String HEADER_API_SECRET = "X-API-Secret";

	@Autowired
	Database database;

	@Override
	public User authenticate(HttpServletRequest request, HttpServletResponse response) {
		String apiKey = request.getHeader(HEADER_API_KEY);
		if (apiKey == null) {
			return null;
		}
		String apiSecret = request.getHeader(HEADER_API_SECRET);
		if (apiSecret == null) {
			throw new APIAuthenticationException(HEADER_API_SECRET, "Missing http header: " + HEADER_API_SECRET);
		}
		ApiAuth aa = database.fetch("select * from ApiAuth where apiKey=? and apiSecret=?", apiKey, apiSecret);
		if (aa == null || aa.disabled) {
			throw new APIAuthenticationException("Invalid " + HEADER_API_KEY + " or " + HEADER_API_SECRET);
		}
		return database.get(User.class, aa.userId);
	}

}
