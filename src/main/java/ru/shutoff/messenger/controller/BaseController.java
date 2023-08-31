package ru.shutoff.messenger.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.shutoff.messenger.exception.DuplicateUserException;
import ru.shutoff.messenger.exception.InvalidTokenException;

@ControllerAdvice
public class BaseController {
	@ExceptionHandler(DuplicateUserException.class)
	public ResponseEntity<String> handleDuplicateUserException(DuplicateUserException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<String> handleDuplicateUserException(InvalidTokenException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}
}
