package com.itranswarp.shici.web;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.itranswarp.shici.exception.APIErrorInfo;
import com.itranswarp.shici.exception.APIException;

@ControllerAdvice
public class RestExceptionHandler {

	final Log log = LogFactory.getLog(getClass());

	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(Exception.class)
	@ResponseBody
	APIErrorInfo handleException(HttpServletRequest request, Exception e) throws Exception {
		if (request.getRequestURI().startsWith("/api/")) {
			if (e instanceof APIException) {
				log.error("Handle APIException: " + e.getClass().getName());
				return ((APIException) e).toErrorInfo();
			}
			if (e instanceof EntityNotFoundException) {
				EntityNotFoundException enf = (EntityNotFoundException) e;
				return new APIErrorInfo("entity:notfound", enf.getMessage(), "Entity not found: " + enf.getMessage());
			}
			log.error("Handle Non-APIException: " + e.getClass().getName(), e);
			return new APIException(e).toErrorInfo();
		}
		throw e;
	}

}
