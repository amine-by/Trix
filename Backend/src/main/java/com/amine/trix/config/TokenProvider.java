package com.amine.trix.config;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.amine.trix.security.UserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Service
public class TokenProvider {
	private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

	@Value("${app.jwt.expiration}")
	private Long jwtExpiration;

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	public String createToken(Authentication authentication) {

		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

		return Jwts.builder().setSubject(userPrincipal.getId()).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
				.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)), SignatureAlgorithm.HS512).compact();
	}

	public String getUserIdFromToken(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
				.build().parseClaimsJws(token).getBody();

		return claims.getSubject();
	}

	public boolean validateToken(String authToken) {
		try {
			Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))).build()
					.parseClaimsJws(authToken).getBody();
			return true;
		} catch (SignatureException ex) {
			logger.error("Invalid JWT signature");
		} catch (MalformedJwtException ex) {
			logger.error("Invalid JWT token");
		} catch (ExpiredJwtException ex) {
			logger.error("Expired JWT token");
		} catch (UnsupportedJwtException ex) {
			logger.error("Unsupported JWT token");
		} catch (IllegalArgumentException ex) {
			logger.error("JWT claims string is empty.");
		}
		return false;
	}
}
