package ru.shutoff.messenger.domain.chat_logic.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.chat_logic.dto.AddUserToChatRoomRequest;
import ru.shutoff.messenger.domain.chat_logic.dto.ChatRoomInfoRequest;
import ru.shutoff.messenger.domain.chat_logic.dto.ChatRoomInfoResponse;
import ru.shutoff.messenger.domain.chat_logic.dto.ChatRoomResponse;
import ru.shutoff.messenger.domain.chat_logic.dto.CreateChatRoomRequest;
import ru.shutoff.messenger.domain.chat_logic.dto.DeleteChatRoomRequest;
import ru.shutoff.messenger.domain.chat_logic.dto.DeleteUserFromChatRoomRequest;
import ru.shutoff.messenger.domain.chat_logic.model.ChatRoom;
import ru.shutoff.messenger.domain.chat_logic.service.ChatRoomService;
import ru.shutoff.messenger.domain.user_mgmt.security.JwtUtils;

@RestController
@RequestMapping("/chatRoom")
@RequiredArgsConstructor
public class ChatRoomController {
    private final @NonNull JwtUtils jwtUtils;
    private final @NonNull ChatRoomService chatRoomService;

    @PostMapping
    public ChatRoomResponse addChatRoom(
        @RequestBody CreateChatRoomRequest chatRoomRequest,
		@CookieValue(name = JwtUtils.JwtCookieName, required = false) Cookie jwtCookie,
        HttpServletResponse response
    ) {
        List<UUID> users = chatRoomRequest.users().stream().map(UUID::fromString).toList();
        UUID chatRoomId = chatRoomService.addChatRoom(chatRoomRequest.name(), users, chatRoomRequest.creatorId());
		jwtUtils.refreshJwtToken(jwtCookie);
		response.addCookie(jwtCookie);
        return new ChatRoomResponse(chatRoomId, chatRoomRequest.creatorId(), users, chatRoomRequest.name());
    }

    @PostMapping("/user")
    public ChatRoomResponse addUserToChatRoom(
        @RequestBody AddUserToChatRoomRequest chatRoomRequest,
		@CookieValue(name = JwtUtils.JwtCookieName, required = false) Cookie jwtCookie,
        HttpServletResponse httpResponse
    ) {
        ChatRoom chatRoom = chatRoomService.addUserToChatRoom(chatRoomRequest.chatRoomId(), chatRoomRequest.userId(), chatRoomRequest.requesterId());
        List<UUID> users = chatRoomService.getChatRoomParticipants(chatRoomRequest.chatRoomId());
		ChatRoomResponse response = new ChatRoomResponse(chatRoom.getChatRoomId(), chatRoom.getCreatorId(), users, chatRoom.getName());
		jwtUtils.refreshJwtToken(jwtCookie);
		httpResponse.addCookie(jwtCookie);
        return response;
    }

    @DeleteMapping("/user")
    public ChatRoomResponse deleteUserFromChatRoom(
        @RequestBody DeleteUserFromChatRoomRequest chatRoomRequest,
		@CookieValue(name = JwtUtils.JwtCookieName, required = false) Cookie jwtCookie,
        HttpServletResponse httpResponse
    ) {
        ChatRoom chatRoom = chatRoomService.deleteUserFromChatRoom(chatRoomRequest.chatRoomId(), chatRoomRequest.userId(), chatRoomRequest.requesterId());
        List<UUID> users = chatRoomService.getChatRoomParticipants(chatRoomRequest.chatRoomId());
		ChatRoomResponse response = new ChatRoomResponse(chatRoom.getChatRoomId(), chatRoom.getCreatorId(), users, chatRoom.getName());
        jwtUtils.refreshJwtToken(jwtCookie);
		httpResponse.addCookie(jwtCookie);
        return response;
    }

    @PatchMapping("/info")
    public void updateChatRoomInfo(
        @RequestBody ChatRoomInfoRequest chatRoomInfoRequest,
		@CookieValue(name = JwtUtils.JwtCookieName, required = false) Cookie jwtCookie,
        HttpServletResponse response
    ) {
        UUID chatRoomId = UUID.fromString(chatRoomInfoRequest.chatRoomId());
        UUID userId = UUID.fromString(jwtUtils.getUsernameFromJwtToken(jwtCookie.getValue()));
        chatRoomService.updateChatRoomInfo(chatRoomId, chatRoomInfoRequest.name(), chatRoomInfoRequest.description(), userId);
		jwtUtils.refreshJwtToken(jwtCookie);
		response.addCookie(jwtCookie);
    }

    @DeleteMapping
    public void deleteChatRoom(
        @RequestBody DeleteChatRoomRequest chatRoomRequest,
		@CookieValue(name = JwtUtils.JwtCookieName, required = false) Cookie jwtCookie,
        HttpServletResponse response
    ) {
        chatRoomService.deleteChatRoom(chatRoomRequest.chatRoomId(), chatRoomRequest.requesterId());
		jwtUtils.refreshJwtToken(jwtCookie);
		response.addCookie(jwtCookie);
    }

    @GetMapping
    public ChatRoomResponse getChatRoom(
        @RequestParam @NonNull String chatRoomId,
		@CookieValue(name = JwtUtils.JwtCookieName, required = false) Cookie jwtCookie,
        HttpServletResponse httpResponse
    ) {
        UUID _chatRoomId = UUID.fromString(chatRoomId);
        UUID userId = UUID.fromString(jwtUtils.getUsernameFromJwtToken(jwtCookie.getValue()));
        ChatRoom chatRoom = chatRoomService.getChatRoom(_chatRoomId, userId);
        List<UUID> users = chatRoomService.getChatRoomParticipants(_chatRoomId);
        ChatRoomResponse response = new ChatRoomResponse(chatRoom.getChatRoomId(), chatRoom.getCreatorId(), users, chatRoom.getName());
		jwtUtils.refreshJwtToken(jwtCookie);
		httpResponse.addCookie(jwtCookie);
        return response;
    }

    @GetMapping("/info")
    public ChatRoomInfoResponse getChatRoomInfo(
        @RequestParam @NonNull String chatRoomId,
		@CookieValue(name = JwtUtils.JwtCookieName, required = false) Cookie jwtCookie,
        HttpServletResponse httpResponse
    ) {
        UUID _chatRoomId = UUID.fromString(chatRoomId);
        UUID userId = UUID.fromString(jwtUtils.getUsernameFromJwtToken(jwtCookie.getValue()));
        ChatRoom chatRoom = chatRoomService.getChatRoom(_chatRoomId, userId);
        List<UUID> users = chatRoomService.getChatRoomParticipants(_chatRoomId);
        ChatRoomInfoResponse response = new ChatRoomInfoResponse(chatRoom.getChatRoomId(), chatRoom.getCreatorId(), users, chatRoom.getName(), chatRoom.getDescription());
		jwtUtils.refreshJwtToken(jwtCookie);
		httpResponse.addCookie(jwtCookie);
        return response;
    }
}
