package com.itranswarp.shici.exception;

public class InvalidJobStatusException extends RuntimeException {

	public InvalidJobStatusException(String message) {
		super(message);
	}

	public InvalidJobStatusException(Throwable t) {
		super(t);
	}

	public InvalidJobStatusException(String message, Throwable t) {
		super(message, t);
	}

}
