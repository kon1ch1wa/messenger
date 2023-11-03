package ru.shutoff.messenger.chat_logic.exception;

public class ChatRoomNotFoundException extends RuntimeException {
	public ChatRoomNotFoundException(String message) {
		super(message);
	}
}
