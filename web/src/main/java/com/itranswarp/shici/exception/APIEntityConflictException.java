package com.itranswarp.shici.exception;

public class APIEntityConflictException extends APIException {

	public APIEntityConflictException(String name, String message) {
		super("entity:conflict", name, message);
	}

	public APIEntityConflictException(Class<?> clazz) {
		super("entity:conflict", clazz.getSimpleName(), "Entity conflict.");
	}

}
