package com.itranswarp.shici.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Record performace of request.
 * 
 * @author michael
 */
@Component
public class PerformanceHandler extends HandlerInterceptorAdapter {

	static final String ATTR_START_TIME = "ATTR_START_TIME";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		request.setAttribute(ATTR_START_TIME, System.currentTimeMillis());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		long startTime = (Long) request.getAttribute(ATTR_START_TIME);
		long execTime = System.currentTimeMillis() - startTime;
		response.addHeader("X-Execution-Time", execTime + " ms");
	}

}
