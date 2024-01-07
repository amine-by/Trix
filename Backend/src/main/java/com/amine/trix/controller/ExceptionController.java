package com.amine.trix.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.amine.trix.exception.InvalidGameException;
import com.amine.trix.exception.InvalidMoveException;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.exception.UserAlreadyInGameException;
import com.amine.trix.exception.UserIsNotInGameException;

@ControllerAdvice
public class ExceptionController {
	
	@ExceptionHandler(InvalidGameException.class)
    public ResponseEntity<String> handleInvalidGameException(InvalidGameException e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(InvalidMoveException.class)
    public ResponseEntity<String> handleInvalidMoveException(InvalidMoveException e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(InvalidParamException.class)
    public ResponseEntity<String> handleInvalidParamException(InvalidParamException e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(UserAlreadyInGameException.class)
    public ResponseEntity<String> handleUserAlreadyInGameException(UserAlreadyInGameException e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(UserIsNotInGameException.class)
    public ResponseEntity<String> handleUserIsNotInGameException(UserIsNotInGameException e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> handleInterruptedException() {
        return new ResponseEntity<String>("Service unavailable - Thread interrupted", HttpStatus.SERVICE_UNAVAILABLE);
    }
	
}
