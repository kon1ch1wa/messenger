package ru.shutoff.messenger.exception;

public class NotAuthorizedException extends RuntimeException {
	public NotAuthorizedException(String message) {
		super(message);
	}
}
