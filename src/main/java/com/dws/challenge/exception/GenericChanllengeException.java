package com.dws.challenge.exception;

import lombok.Data;

@Data
public class GenericChanllengeException extends RuntimeException {

	private static final long serialVersionUID = -1774247559369370738L;

	private String message;
	
	public GenericChanllengeException(String message) {
		super(message);
		this.message = message;
	}

	public GenericChanllengeException() {
		super();
	}

	public GenericChanllengeException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
	}

	public GenericChanllengeException(Throwable cause) {
		super(cause);
	}

}
