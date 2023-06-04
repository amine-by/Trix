package com.amine.trix.config.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.amine.trix.config.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

	private final static Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);
	private final JwtProvider jwtProvider;
	private final UserDetailsServiceImpl userDetailsServiceImpl;

	private String getToken(HttpServletRequest request) {
		String authRequest = request.getHeader("Authorization");
		if (authRequest != null && authRequest.startsWith("Bearer "))
			return authRequest.replace("Bearer ", "");
		return null;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String token = getToken(request);
			String id = jwtProvider.getIdFromToken(token);
			UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(id);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(id,
					userDetails);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (Exception e) {
			logger.error("problem in the doFilter method");
		}

		filterChain.doFilter(request, response);
	}

}
