package com.itranswarp.shici.exception;

public class APIArgumentException extends APIException {

	public APIArgumentException(String field) {
		super("argument:invalid", field, "Invalid " + field + ".");
	}

	public APIArgumentException(String field, String message) {
		super("argument:invalid", field, message);
	}

}
