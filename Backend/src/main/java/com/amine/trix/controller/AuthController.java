package com.amine.trix.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amine.trix.dto.AuthDto;
import com.amine.trix.dto.LoginDto;
import com.amine.trix.dto.RegisterDto;
import com.amine.trix.exception.BadRequestException;
import com.amine.trix.service.AuthService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<AuthDto> authenticateUser(@RequestBody LoginDto loginDto) {
		return new ResponseEntity<AuthDto>(authService.authenticateUser(loginDto), new HttpHeaders(), 200);
	}
	
	@PostMapping("/register")
	public ResponseEntity<AuthDto> registerUser(@RequestBody RegisterDto registerDto) throws BadRequestException {
		return new ResponseEntity<AuthDto>(authService.registerUser(registerDto), new HttpHeaders(), 201);
	}
}
