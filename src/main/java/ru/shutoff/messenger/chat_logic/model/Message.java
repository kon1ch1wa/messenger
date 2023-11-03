package ru.shutoff.messenger.chat_logic.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.sql.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Message {
	@Id
	private String messageId;
	private String content;
	private String senderId;
	private String receiverId;
	private String chatRoomId;
	private Date sendDate;
}
