package com.itranswarp.shici.exception;

public class APIErrorInfo {

	public final String error;
	public final String field;
	public final String message;

	public APIErrorInfo(String error, String field, String message) {
		this.error = error;
		this.field = field;
		this.message = message;
	}

}
