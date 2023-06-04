package com.amine.trix.service;


import java.util.HashSet;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;

import com.amine.trix.config.jwt.JwtProvider;
import com.amine.trix.dto.TokenDto;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.model.Role;
import com.amine.trix.model.User;
import com.amine.trix.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

	private final AuthenticationManager authenticationManager;
	private final JwtProvider jwtProvider;
	private final UserRepository userRepository;

	private User save(String appId, String name) {
		User user = new User();
		HashSet<Role> roles = new HashSet<Role>();
		user.setAppId(appId);
		user.setName(name);
		user.setRoles(roles);
		return userRepository.save(user);
	}
	
	private TokenDto login(User user) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(user.getId(), null));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtProvider.generateToken(authentication);
		TokenDto tokenDto = new TokenDto();
		tokenDto.setValue(jwt);
		return tokenDto;
	}

	public TokenDto facebook(TokenDto tokenRequest) throws InvalidParamException {
		Facebook facebook = new FacebookTemplate(tokenRequest.getValue());
		org.springframework.social.facebook.api.User fbUser = facebook.fetchObject("me",
				org.springframework.social.facebook.api.User.class);
		User user = new User();
		if (userRepository.existsByAppId(fbUser.getId()))
			user = userRepository.findByAppId(fbUser.getId())
					.orElseThrow(() -> new InvalidParamException("User does not exist"));
		else
			user = save(fbUser.getId(),fbUser.getName());
		TokenDto tokenResponse = login(user);
		return tokenResponse;
	}
}
