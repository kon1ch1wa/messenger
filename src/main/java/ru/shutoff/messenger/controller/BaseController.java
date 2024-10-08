package ru.shutoff.messenger.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ru.shutoff.messenger.domain.chat_logic.exception.ChatRoomNotFoundException;
import ru.shutoff.messenger.domain.chat_logic.exception.MessageNotFoundException;
import ru.shutoff.messenger.domain.file_handling.exception.InvalidFileDataException;
import ru.shutoff.messenger.domain.file_handling.exception.InvalidMinioCredentialsException;
import ru.shutoff.messenger.domain.user_mgmt.exception.DuplicateUserException;
import ru.shutoff.messenger.domain.user_mgmt.exception.InvalidTokenException;
import ru.shutoff.messenger.domain.user_mgmt.exception.NotAuthorizedException;

@ControllerAdvice
public class BaseController {
	@ExceptionHandler(DuplicateUserException.class)
	public ResponseEntity<String> handleDuplicateUserException(DuplicateUserException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}
	@ExceptionHandler(NotAuthorizedException.class)
	public ResponseEntity<String> handleNotAuthorizedException(NotAuthorizedException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
	}
	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<String> handleNotFoundException(UsernameNotFoundException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
	}
	@ExceptionHandler(MessageNotFoundException.class)
	public ResponseEntity<String> handleNotFoundException(MessageNotFoundException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
	}
	@ExceptionHandler(ChatRoomNotFoundException.class)
	public ResponseEntity<String> handleNotFoundException(ChatRoomNotFoundException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
	}
	@ExceptionHandler(InvalidFileDataException.class)
	public ResponseEntity<String> handleInvalidFileDataExceptionException(InvalidFileDataException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}
	@ExceptionHandler(InvalidMinioCredentialsException.class)
	public ResponseEntity<String> handleInvalidMinioCredentialsExceptionException(InvalidMinioCredentialsException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
