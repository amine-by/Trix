package com.amine.trix.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.amine.trix.model.User;
import com.amine.trix.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String appId) throws UsernameNotFoundException {
		User user = userRepository.findByAppId(appId)
				.orElseThrow(() -> new UsernameNotFoundException("facebook Id not found"));
		return UserPrincipalFactory.build(user);
	}
}
