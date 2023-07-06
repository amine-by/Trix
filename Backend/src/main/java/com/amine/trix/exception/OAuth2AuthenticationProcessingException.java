package com.amine.trix.exception;

import org.springframework.security.core.AuthenticationException;

public class OAuth2AuthenticationProcessingException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public OAuth2AuthenticationProcessingException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public OAuth2AuthenticationProcessingException(String message) {
		super(message);
	}
}
