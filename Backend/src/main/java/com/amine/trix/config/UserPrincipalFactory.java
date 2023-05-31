package com.amine.trix.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.amine.trix.model.User;

public class UserPrincipalFactory {
	public static UserPrincipal build(User user) {
		List<GrantedAuthority> authorities = user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toList());
		return new UserPrincipal(user.getId(), null, authorities);
	}
}