package com.itranswarp.shici.exception;

public class InvalidPaymentParameterException extends PaymentException {

	public InvalidPaymentParameterException(String message) {
		super(message);
	}

	public InvalidPaymentParameterException(Throwable t) {
		super(t);
	}

	public InvalidPaymentParameterException(String message, Throwable t) {
		super(message, t);
	}

}
