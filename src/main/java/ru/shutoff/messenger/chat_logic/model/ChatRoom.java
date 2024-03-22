package ru.shutoff.messenger.chat_logic.model;

import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.shutoff.messenger.chat_logic.dto.ChatRoomInfoResponse;
import ru.shutoff.messenger.chat_logic.dto.ChatRoomResponse;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatRoom {
	@NotNull
	private UUID chatRoomId;

	@Nullable
	private String name;

	@Nullable
	private String description;

	public ChatRoomResponse toResponse() {
		String _chatRoomId = chatRoomId.toString();
		if (_chatRoomId == null) {
			throw new NullPointerException("ChatRoomId is null");
		}
		return new ChatRoomResponse(_chatRoomId, name);
	}

	public ChatRoomInfoResponse toInfoResponse() {
		String _chatRoomId = chatRoomId.toString();
		if (_chatRoomId == null) {
			throw new NullPointerException("ChatRoomId is null");
		}
		return new ChatRoomInfoResponse(_chatRoomId, name, description);
	}
}
