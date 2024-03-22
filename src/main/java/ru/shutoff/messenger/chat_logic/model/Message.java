package ru.shutoff.messenger.chat_logic.model;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.shutoff.messenger.chat_logic.dto.MessageDto;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Message {
	private Long messageId;
	private String content;
	private UUID senderId;
	private UUID chatRoomId;
	private Timestamp sendDate;

	@Override
	public String toString() {
		return
			this.getSenderId() + " sent " +
			this.getContent() + " to " +
			this.getChatRoomId();
	}

	public MessageDto toDto() {
		String _senderId = senderId.toString();
		String _chatRoomId = chatRoomId.toString();
		if (_senderId == null || _chatRoomId == null) {
			throw new NullPointerException("Invalid senderId or chatRoomId");
		}
		return new MessageDto(content, _senderId, _chatRoomId, sendDate);
	}
}
