package com.amine.trix.exception;

public class UserIsNotInGameException extends Exception {
	private static final long serialVersionUID = 1L;

	public UserIsNotInGameException(String message) {
		super(message);
	}

}
