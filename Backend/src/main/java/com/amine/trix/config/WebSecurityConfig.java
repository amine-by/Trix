package com.amine.trix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.amine.trix.config.jwt.JwtEntryPoint;
import com.amine.trix.config.jwt.JwtProvider;
import com.amine.trix.config.jwt.JwtTokenFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	private UserDetailsServiceImpl userDetailsServiceImpl;
	private JwtProvider jwtProvider;
	private JwtEntryPoint jwtEntryPoint;

	@Bean
	JwtTokenFilter jwtTokenFilter() {
		return new JwtTokenFilter(jwtProvider, userDetailsServiceImpl);
	}

	@Bean
	AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		return http.getSharedObject(AuthenticationManagerBuilder.class).build();
	}

	@Bean
	SecurityFilterChain configure(HttpSecurity http) throws Exception {
		return http.cors((c) -> c.disable()).csrf((c) -> c.disable()).authorizeHttpRequests((a) -> {
			a.requestMatchers("/api/auth/**").permitAll();
			a.anyRequest().authenticated();
		}).exceptionHandling((e) -> e.authenticationEntryPoint(jwtEntryPoint))
				.sessionManagement((s) -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class).build();
	}

}
