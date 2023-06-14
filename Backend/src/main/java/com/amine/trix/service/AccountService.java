package com.amine.trix.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;

import com.amine.trix.dto.TokenDto;
import com.amine.trix.enums.Role;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.model.Account;
import com.amine.trix.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	@Value("${default.password}")
	private String DEFAULT_PASSWORD;
	private final AuthenticationManager authenticationManager;
	private final AccountRepository accountRepository;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;

	public TokenDto facebook(TokenDto tokenRequest) throws InvalidParamException {
		Facebook facebook = new FacebookTemplate(tokenRequest.getValue());
		User user = facebook.fetchObject("me", User.class);
		Account account = new Account();
		if (accountRepository.existsByAppId(user.getId()))
			account = accountRepository.findByAppId(user.getId())
					.orElseThrow(() -> new InvalidParamException("account does not exist"));
		else
			account = save(user.getId(), user.getName());
		TokenDto tokenResponse = login(account);
		return tokenResponse;
	}

	private Account save(String appId, String name) {
		Account account = new Account();
		account.setAppId(appId);
		account.setName(name);
		account.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
		account.setRole(Role.USER);
		return accountRepository.save(account);
	}

	private TokenDto login(Account account) {
		try {
			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(account.getId(), DEFAULT_PASSWORD));
			TokenDto token = new TokenDto();
			token.setValue(jwtService.generateToken(account));
			return token;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
