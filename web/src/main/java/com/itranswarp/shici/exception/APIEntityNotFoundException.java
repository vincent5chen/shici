package com.itranswarp.shici.exception;

public class APIEntityNotFoundException extends APIException {

	public APIEntityNotFoundException(String name, String message) {
		super("entity:notfound", name, message);
	}

	public APIEntityNotFoundException(Class<?> clazz, String message) {
		super("entity:notfound", clazz.getSimpleName(), message);
	}

	public APIEntityNotFoundException(Class<?> clazz) {
		super("entity:notfound", clazz.getSimpleName(), "Entity not found.");
	}

}
