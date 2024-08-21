package ru.shutoff.messenger.domain.user_mgmt.exception;

public class InvalidTokenException extends RuntimeException {
	public InvalidTokenException(String message) {
		super(message);
	}
}
