package com.itranswarp.shici.exception;

public class APIEntityNotReadyException extends APIException {

	public APIEntityNotReadyException(String name, String message) {
		super("entity:notready", name, message);
	}

	public APIEntityNotReadyException(Class<?> clazz) {
		super("entity:notready", clazz.getSimpleName(), "Entity not ready.");
	}

}
