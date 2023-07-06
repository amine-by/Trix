package com.amine.trix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.amine.trix.security.CustomOAuth2UserService;
import com.amine.trix.security.CustomUserDetailsService;
import com.amine.trix.security.TokenAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
	private final TokenAuthenticationFilter tokenAuthenticationFilter;
	private final CustomUserDetailsService customUserDetailsService;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(customUserDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	SecurityFilterChain configure(HttpSecurity http) throws Exception {
		return http.cors(cors -> cors.disable()).csrf(csrf -> csrf.disable()).formLogin(fl -> fl.disable())
				.httpBasic(httpBasic -> httpBasic.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/oauth2/**").permitAll().anyRequest().authenticated())
				.oauth2Login(oauth -> {
					oauth.authorizationEndpoint(ae -> {
						ae.baseUri("/oauth2/authorization");
						ae.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository);
					});
					oauth.redirectionEndpoint(re -> re.baseUri("/oauth2/callback/*"));
					oauth.userInfoEndpoint(uie -> uie.userService(customOAuth2UserService));
					oauth.successHandler(oAuth2AuthenticationSuccessHandler);
					oauth.failureHandler(oAuth2AuthenticationFailureHandler);
				})
				.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class).build();
	}
}
