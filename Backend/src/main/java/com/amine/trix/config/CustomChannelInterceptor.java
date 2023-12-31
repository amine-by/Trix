package com.amine.trix.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.amine.trix.security.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomChannelInterceptor implements ChannelInterceptor {

	private final TokenProvider tokenProvider;
	private final CustomUserDetailsService customUserDetailsService;
	private static final Logger logger = LoggerFactory.getLogger(CustomChannelInterceptor.class);

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		try {
			final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
			String jwt = getJwtFromMessage(accessor);
			if (jwt != null && tokenProvider.validateToken(jwt)) {
				String userId = tokenProvider.getUserIdFromToken(jwt);
				UserDetails userDetails = customUserDetailsService.loadUserById(userId);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				accessor.setUser(authentication);
			}
		} catch (Exception e) {
			logger.error("Token is invalid", e);
		}
		return message;
	}

	private String getJwtFromMessage(StompHeaderAccessor accessor) {
		String bearerToken = accessor.getFirstNativeHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7, bearerToken.length());
		}
		return null;
	}
}
