package com.amine.trix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amine.trix.dto.TokenDto;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.service.AccountService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
	
	private final AccountService accountService;
	
	@PostMapping("/facebook")
	public ResponseEntity<TokenDto> facebook(@RequestBody TokenDto tokenDto) throws InvalidParamException{
		return ResponseEntity.ok(accountService.facebook(tokenDto));
	}
}
