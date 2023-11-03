package ru.shutoff.messenger.chat_logic.service;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.shutoff.messenger.chat_logic.exception.ChatRoomNotFoundException;
import ru.shutoff.messenger.chat_logic.model.ChatRoom;
import ru.shutoff.messenger.chat_logic.model.Message;
import ru.shutoff.messenger.chat_logic.repository.ChatRoomRepo;
import ru.shutoff.messenger.chat_logic.repository.MessageRepo;
import ru.shutoff.messenger.exception.NotAuthorizedException;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.repository.UserInfoRepo;
import ru.shutoff.messenger.security.JwtUtils;

import java.sql.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final SimpMessagingTemplate rabbitMQSender;
	private final UserInfoRepo userInfoRepo;
	private final MessageRepo messageRepo;
	private final ChatRoomRepo chatRoomRepo;
	private final JwtUtils jwtUtils;

	public void sendMessage(Cookie senderJwtToken, String content, String receiverId, Date sendDate, String chatRoomId) {
		String username = jwtUtils.getUsernameFromJwtToken(senderJwtToken.getValue());
		User user = userInfoRepo.getByLogin(username);
		String senderId = user.getId();
		ChatRoom chatRoom = chatRoomRepo.getById(chatRoomId);
		if (!Objects.equals(senderId, chatRoom.getFirstParticipantId()) && !Objects.equals(senderId, chatRoom.getSecondParticipantId())) {
			throw new NotAuthorizedException("Not authorized");
		}
		if (!Objects.equals(receiverId, chatRoom.getFirstParticipantId()) && !Objects.equals(receiverId, chatRoom.getSecondParticipantId())) {
			throw new NotAuthorizedException("Not authorized");
		}
		Message message = Message.builder()
				.messageId(UUID.randomUUID().toString())
				.content(content)
				.senderId(senderId)
				.receiverId(receiverId)
				.chatRoomId(chatRoomId)
				.sendDate(sendDate)
				.build();
		messageRepo.save(message);
		rabbitMQSender.convertAndSend("/queue/messages/" + chatRoomId, content);
	}

	public User getUser(String urlTag) {
		User user = null;
		try {
			user = userInfoRepo.getByUrlTag(urlTag);
		} catch (UsernameNotFoundException ex) {
			user = userInfoRepo.getById(urlTag);
		}
		return user;
	}

	public ChatRoom getChatRoom(Cookie senderJwtToken, String participantAttr2) {
		String username = jwtUtils.getUsernameFromJwtToken(senderJwtToken.getValue());
		User user1 = userInfoRepo.getByLogin(username);
		User user2 = null;
		try {
			user2 = userInfoRepo.getByUrlTag(participantAttr2);
		} catch (UsernameNotFoundException ex) {
			user2 = userInfoRepo.getById(participantAttr2);
		}
		ChatRoom chatRoom = null;
		try {
			chatRoom = chatRoomRepo.getByParticipants(user1.getId(), user2.getId());
		} catch (ChatRoomNotFoundException ex) {
			chatRoom = ChatRoom.builder()
					.chatRoomId(Stream.of(user1.getId(), user2.getId()).sorted().reduce((f, s) -> f + "," + s).get())
					.firstParticipantId(user1.getId())
					.secondParticipantId(user2.getId())
					.build();
			chatRoomRepo.save(chatRoom);
		}
		return chatRoom;
	}

	public List<Message> getMessages(Cookie senderJwtToken, String chatRoomId) {
		try {
			String username = jwtUtils.getUsernameFromJwtToken(senderJwtToken.getValue());
			User user = userInfoRepo.getByLogin(username);
			ChatRoom chatRoom = chatRoomRepo.getById(chatRoomId);
			if (!Objects.equals(user.getId(), chatRoom.getFirstParticipantId()) && !Objects.equals(user.getId(), chatRoom.getSecondParticipantId())) {
				throw new NotAuthorizedException("Not authorized");
			}
			return messageRepo.getByChatRoomId(chatRoomId);
		} catch (UsernameNotFoundException | NotAuthorizedException ex) {
			throw new NotAuthorizedException("Not authorized");
		}
	}
}
