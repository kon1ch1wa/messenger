package ru.shutoff.messenger.domain.chat_logic.exception;

public class ForbiddenToPerformActionException extends RuntimeException {
	public ForbiddenToPerformActionException(String message) {
		super(message);
	}
}
