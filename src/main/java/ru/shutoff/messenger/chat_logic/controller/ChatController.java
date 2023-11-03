package ru.shutoff.messenger.chat_logic.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.shutoff.messenger.chat_logic.dto.MessageDto;
import ru.shutoff.messenger.chat_logic.model.ChatRoom;
import ru.shutoff.messenger.chat_logic.model.Message;
import ru.shutoff.messenger.chat_logic.service.ChatService;
import ru.shutoff.messenger.exception.NotAuthorizedException;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.repository.UserInfoRepo;
import ru.shutoff.messenger.security.JwtUtils;

import java.sql.Date;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
	private final JwtUtils jwtUtils;
	private final ChatService chatService;

	@PostMapping("/chat/{chatRoomId}/send-message")
	public void sendMessage(
			@PathVariable String chatRoomId,
			@RequestBody MessageDto message,
			HttpServletRequest request,
			HttpServletResponse response
	) {
		Cookie cookie = jwtUtils.getJwtCookieFromRequest(request);
		chatService.sendMessage(cookie, message.content(), message.receiverId(), new Date(System.currentTimeMillis()), chatRoomId);
		jwtUtils.refreshJwtToken(cookie.getValue(), cookie);
		response.addCookie(cookie);
	}

	@GetMapping("/chat/{urlTag}/info")
	@ResponseBody
	public User getInfoAboutUser(
			@PathVariable String urlTag
	) {
		return chatService.getUser(urlTag);
	}

	@GetMapping("/chat/{receiverUrlTag}/room")
	@ResponseBody
	public ChatRoom getChatRoom(
			@PathVariable String receiverUrlTag,
			HttpServletRequest request
	) {
		Cookie cookie = jwtUtils.getJwtCookieFromRequest(request);
		return chatService.getChatRoom(cookie, receiverUrlTag);
	}

	@GetMapping("/chat/{chatRoomId}/messages")
	@ResponseBody
	public List<Message> getMessages(
			@PathVariable String chatRoomId,
			HttpServletRequest request
	) {
		Cookie cookie = jwtUtils.getJwtCookieFromRequest(request);
		return chatService.getMessages(cookie, chatRoomId);
	}
}
