package ru.shutoff.messenger.domain.chat_logic.exception;

public class UserAndChatRoomException extends RuntimeException {
	public UserAndChatRoomException(String message) {
		super(message);
	}
}
