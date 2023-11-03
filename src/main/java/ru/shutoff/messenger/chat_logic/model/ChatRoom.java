package ru.shutoff.messenger.chat_logic.model;

import lombok.*;
import org.springframework.data.annotation.Id;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChatRoom {
	@Id
	private String chatRoomId;
	private String firstParticipantId;
	private String secondParticipantId;
}
