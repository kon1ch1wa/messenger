package ru.shutoff.messenger.chat_logic.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record MessageDto(
	String chatRoomId,
	String senderId,
	String content,
	Timestamp sendDate
) {
}
