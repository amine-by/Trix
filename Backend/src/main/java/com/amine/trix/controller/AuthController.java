package com.amine.trix.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amine.trix.payload.request.TokenRequest;

@RestController
@RequestMapping("/api/oauth")	
public class AuthController {
	@PostMapping("/facebook")
	public ResponseEntity<Object> facebook(@RequestBody TokenRequest tokenRequest){
		Facebook facebook = new FacebookTemplate(tokenRequest.getValue());
		User user = facebook.fetchObject("me", User.class);
		return new ResponseEntity<Object>(user, HttpStatus.OK);
	}
}
