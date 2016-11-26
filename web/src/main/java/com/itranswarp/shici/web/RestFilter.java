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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

/**
 * Do rest api for each request.
 * 
 * @author michael
 */
@Component("restFilter")
public class RestFilter implements Filter {

	final Log log = LogFactory.getLog(getClass());

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		if (request.getHeader("Host").startsWith("localhost")) {
			log.info("Sleep 0.3 second in localhost mode...");
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		String httpMethod = request.getMethod();
		log.info("Handle REST request: " + httpMethod + " " + request.getRequestURI());
		if (!"GET".equals(httpMethod) && !"OPTIONS".equals(httpMethod)) {
			// check Content-Type:
			if (request.getContentLength() != 0) {
				String contentType = request.getContentType();
				if (contentType != null && !contentType.toLowerCase().startsWith("application/json")) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
			}
		}
		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("Init RestFilter...");
	}

	@Override
	public void destroy() {
		log.info("Destroy RestFilter...");
	}
}
