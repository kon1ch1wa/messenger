package ru.shutoff.messenger.domain.chat_logic.model;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.shutoff.messenger.domain.chat_logic.dto.MessageDto;

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
		return new MessageDto(chatRoomId, senderId, content, sendDate);
	}
}
