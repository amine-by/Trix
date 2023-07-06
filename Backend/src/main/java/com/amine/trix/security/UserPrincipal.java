package com.amine.trix.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.amine.trix.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter
public class UserPrincipal implements OAuth2User, UserDetails {

	private static final long serialVersionUID = 1L;
	private String id;
	private String email;
	private String password;
	private Collection<? extends GrantedAuthority> authorities;

	@Setter
	private Map<String, Object> attributes;

	public UserPrincipal(String id, String email, String password, List<GrantedAuthority> authorities) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.authorities = authorities;
	}

	public static UserPrincipal create(User user) {

		List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

		return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), authorities);
	}
	
	public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

	@Override
	public String getName() {
		return id;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
