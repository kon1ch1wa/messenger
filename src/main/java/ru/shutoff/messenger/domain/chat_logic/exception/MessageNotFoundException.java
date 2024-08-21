package ru.shutoff.messenger.domain.chat_logic.exception;

public class MessageNotFoundException extends RuntimeException {
	public MessageNotFoundException(String message) {
		super(message);
	}
}
