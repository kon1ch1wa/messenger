package ru.shutoff.messenger.chat_logic.dto;

import java.sql.Timestamp;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record MessageDto(
	UUID chatRoomId,
	UUID senderId,
	String content,
	Timestamp sendDate
) {
}
