package com.itranswarp.shici.exception;

public class PaymentException extends RuntimeException {

	public PaymentException(String message) {
		super(message);
	}

	public PaymentException(Throwable t) {
		super(t);
	}

	public PaymentException(String message, Throwable t) {
		super(message, t);
	}
}
