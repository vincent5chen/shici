package com.itranswarp.shici.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.auth.Authenticator;
import com.itranswarp.shici.context.UserContext;
import com.itranswarp.shici.exception.APIAuthenticationException;
import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.Database;

/**
 * Do authentication for each request and set UserContext if authenticate OK.
 * 
 * @author michael
 */
@Component("authenticationFilter")
public class AuthenticationFilter implements Filter {

	final Log log = LogFactory.getLog(getClass());

	@Autowired
	Database database;

	@Autowired
	Authenticator[] authenticators = new Authenticator[0];

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = new HttpServletResponseWrapper((HttpServletResponse) resp) {
			@Override
			public void sendRedirect(String location) throws IOException {
				super.setStatus(HttpServletResponse.SC_FOUND);
				super.setHeader("Location", location);
			}
		};
		User user = null;
		try {
			for (Authenticator authenticator : this.authenticators) {
				user = authenticator.authenticate(request, response);
				if (user != null) {
					log.info("Authenticate ok.");
					break;
				}
			}
		} catch (APIAuthenticationException e) {
			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
			return;
		}
		if (user == null || user.role > User.Role.EDITOR) {
			if (request.getRequestURI().startsWith("/manage/")) {
				response.sendRedirect("/auth/signin");
				return;
			}
		}
		if (user == null) {
			log.info("No authentication found. Access as anonymous user.");
			try {
				chain.doFilter(request, response);
			} catch (Exception e) {
				log.error("Exception when process request:", e);
				throw new ServletException(e);
			}
		} else {
			log.info("Authentication bind to: " + user.email);
			try (UserContext context = new UserContext(user)) {
				chain.doFilter(request, response);
			} catch (Exception e) {
				log.error("Exception when process request:", e);
				throw new ServletException(e);
			}
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("Init AuthenticationFilter...");
	}

	@Override
	public void destroy() {
		log.info("Destroy AuthenticationFilter...");
	}
}
