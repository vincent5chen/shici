package com.itranswarp.shici.exception;

public class APIPermissionException extends APIException {

	public APIPermissionException() {
		super("permission:denied", null, "Permission denied.");
	}

	public APIPermissionException(String message) {
		super("permission:denied", null, message);
	}

}
