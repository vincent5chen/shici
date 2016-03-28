package com.itranswarp.shici.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.util.IpUtil;

/**
 * Do general filter for each request.
 * 
 * @author michael
 */
@Component("generalFilter")
public class GeneralFilter implements Filter {

	static final String APP_BUILD_UNAVAIABLE = "unavailable";

	static final String NODE_ID = "node-" + IpUtil.ipAddrToIntArray(IpUtil.getIpAddress())[3];

	String build = APP_BUILD_UNAVAIABLE;

	final Log log = LogFactory.getLog(getClass());

	static final String ACCESS_CONTROL_ALLOW_METHODS = String.join(", ",
			Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	static final String ACCESS_CONTROL_ALLOW_HEADERS = String.join(", ",
			Arrays.asList("X-Node", "X-API-Key", "X-API-Secret", "X-Client-Timestamp"));
	static final String ACCESS_CONTROL_MAX_AGE = Integer.toString(3600 * 24 * 7);

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String httpMethod = request.getMethod();
		response.setHeader("X-Node", NODE_ID);
		setCors(httpMethod, request, response);
		chain.doFilter(req, resp);
	}

	void setCors(String httpMethod, HttpServletRequest request, HttpServletResponse response) {
		if ("OPTIONS".equals(httpMethod)) {
			// handle pre-flighted request:
			log.info("handle pre-flighted request: OPTIONS " + request.getRequestURI());
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", ACCESS_CONTROL_ALLOW_METHODS);
			response.setHeader("Access-Control-Allow-Headers", ACCESS_CONTROL_ALLOW_HEADERS);
			response.setHeader("Access-Control-Max-Age", ACCESS_CONTROL_MAX_AGE);
			return;
		} else {
			response.setHeader("Access-Control-Allow-Origin", "*");
		}
	}

	public Object getAppBuild() {
		return this.build;
	}

	String initAppBuild(ServletContext context) {
		String file = context.getRealPath("/META-INF/MANIFEST.MF");
		try (InputStream input = new FileInputStream(file)) {
			Manifest manifest = new Manifest(input);
			Attributes attrs = manifest.getMainAttributes();
			for (Object name : attrs.keySet()) {
				Name key = (Name) name;
				if ("App-Build".equals(key.toString())) {
					Object value = attrs.get(key);
					return value.toString();
				}
			}
		} catch (Exception e) {
			log.warn("Cannot read from MANIFEST.MF");
		}
		return APP_BUILD_UNAVAIABLE;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("Init GeneralFilter...");
		build = initAppBuild(filterConfig.getServletContext());
		log.info("Set App-Build: " + build);
	}

	@Override
	public void destroy() {
		log.info("Destroy GeneralFilter...");
	}

}
