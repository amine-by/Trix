package com.amine.trix.config.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;



@Component
public class JwtProvider {
	
	private final static Logger logger = LoggerFactory.getLogger(JwtEntryPoint.class);

	@Value("${jwt.secret}")
	String secret;
	
	@Value("${jwt.expiration}")
	int expiration;
	
	public String generateToken(Authentication authentication) {
		
	}
	
}
