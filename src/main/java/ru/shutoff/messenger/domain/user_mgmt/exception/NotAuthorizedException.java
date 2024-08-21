package ru.shutoff.messenger.domain.user_mgmt.exception;

public class NotAuthorizedException extends RuntimeException {
	public NotAuthorizedException(String message) {
		super(message);
	}
}
