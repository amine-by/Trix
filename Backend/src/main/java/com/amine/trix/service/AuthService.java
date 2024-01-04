package com.amine.trix.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.amine.trix.config.TokenProvider;
import com.amine.trix.dto.AuthDto;
import com.amine.trix.dto.LoginDto;
import com.amine.trix.dto.RegisterDto;
import com.amine.trix.enums.Provider;
import com.amine.trix.exception.BadRequestException;
import com.amine.trix.model.User;
import com.amine.trix.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenProvider tokenProvider;

	public AuthDto authenticateUser(LoginDto loginDto) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String token = tokenProvider.createToken(authentication);
		return new AuthDto(token);

	}

	public AuthDto registerUser(RegisterDto registerDto) throws BadRequestException {
		if (userRepository.existsByEmail(registerDto.getEmail())) {
			throw new BadRequestException("Email address already in use.");
		}
		User user = new User();
		user.setName(registerDto.getName());
		user.setEmail(registerDto.getEmail());
		user.setPassword(registerDto.getPassword());
		user.setProvider(Provider.local);

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		userRepository.save(user);

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(registerDto.getEmail(), registerDto.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String token = tokenProvider.createToken(authentication);

		return new AuthDto(token);

	}
}
