package com.itranswarp.shici.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itranswarp.shici.model.User;

public interface Authenticator {

	/**
	 * Try to get authenticated User object from current request.
	 * 
	 * @param request
	 *            Current HTTP request.
	 * @return An authenticated user, or null if cannot find authentication
	 *         information.
	 * @throws APIAuthenticationException
	 */
	User authenticate(HttpServletRequest request, HttpServletResponse response);

}
