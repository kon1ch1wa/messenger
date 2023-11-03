package ru.shutoff.messenger.chat_logic.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.validation.constraints.NotNull;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record MessageDto(
		@NotNull
		String content,
		@NotNull
		String receiverId,
		@NotNull
		String chatRoomId
) {
}
