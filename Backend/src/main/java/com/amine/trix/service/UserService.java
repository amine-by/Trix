package com.amine.trix.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.amine.trix.config.UserPrincipalFactory;
import com.amine.trix.model.User;
import com.amine.trix.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
	private final UserRepository userRepository;

	public Optional<User> findByFacebookId(String facebookId) {
		return userRepository.findByFacebookId(facebookId);
	}

	public boolean existsByFacebookId(String facebookId) {
		return userRepository.existsByFacebookId(facebookId);
	}

	public User save(User user) {
		return userRepository.save(user);
	}

	@Override
	public UserDetails loadUserByUsername(String facebookId) throws UsernameNotFoundException {
		User user = findByFacebookId(facebookId)
				.orElseThrow(() -> new UsernameNotFoundException("facebook Id not found"));
		return UserPrincipalFactory.build(user);
	}
}
