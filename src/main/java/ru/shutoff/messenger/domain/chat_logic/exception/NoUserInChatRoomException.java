package ru.shutoff.messenger.domain.chat_logic.exception;

public class NoUserInChatRoomException extends RuntimeException {
	public NoUserInChatRoomException(String message) {
		super(message);
	}
}
