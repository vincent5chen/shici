package com.itranswarp.shici.exception;

public class InvalidPaymentSignatureException extends PaymentException {

	public InvalidPaymentSignatureException(String message) {
		super(message);
	}

	public InvalidPaymentSignatureException(Throwable t) {
		super(t);
	}

	public InvalidPaymentSignatureException(String message, Throwable t) {
		super(message, t);
	}

}
