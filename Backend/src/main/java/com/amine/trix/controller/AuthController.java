package com.amine.trix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amine.trix.dto.TokenDto;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.service.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/api/oauth")
public class AuthController {
	
	private final UserService userService;
	
	@PostMapping("/facebook")
	public ResponseEntity<TokenDto> facebook(@RequestBody TokenDto tokenDto) throws InvalidParamException{
		log.info("connect with facebook account request", tokenDto);
		return ResponseEntity.ok(userService.facebook(tokenDto));
	}
}
