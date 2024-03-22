package ru.shutoff.messenger.chat_logic.controller;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.chat_logic.dto.ChatRoomInfoRequest;
import ru.shutoff.messenger.chat_logic.dto.ChatRoomInfoResponse;
import ru.shutoff.messenger.chat_logic.dto.ChatRoomRequest;
import ru.shutoff.messenger.chat_logic.dto.ChatRoomResponse;
import ru.shutoff.messenger.chat_logic.model.ChatRoom;
import ru.shutoff.messenger.chat_logic.service.ChatRoomService;
import ru.shutoff.messenger.security.JwtUtils;

@RestController
@RequestMapping("/chatRoom")
@RequiredArgsConstructor
public class ChatRoomController {
    private final @NonNull JwtUtils jwtUtils;
    private final @NonNull ChatRoomService chatRoomService;

    @PostMapping
    public String addChatRoom(@RequestBody ChatRoomRequest chatRoomRequest, HttpServletRequest request, HttpServletResponse response) {
        Cookie jwtCookie = jwtUtils.getJwtCookieFromRequest(request);
        UUID chatRoomId = chatRoomService.addChatRoom(chatRoomRequest.name());
		jwtUtils.refreshJwtToken(jwtCookie);
		response.addCookie(jwtCookie);
        return chatRoomId.toString();
    }

    @PatchMapping("/info")
    public void updateChatRoomInfo(@RequestBody ChatRoomInfoRequest chatRoomInfoRequest, HttpServletRequest request, HttpServletResponse response) {
        UUID chatRoomId = UUID.fromString(chatRoomInfoRequest.chatRoomId());
        Cookie jwtCookie = jwtUtils.getJwtCookieFromRequest(request);
        UUID userId = UUID.fromString(jwtUtils.getUsernameFromJwtToken(jwtCookie.getValue()));
        chatRoomService.updateChatRoomInfo(chatRoomId, chatRoomInfoRequest.name(), chatRoomInfoRequest.description(), userId);
		jwtUtils.refreshJwtToken(jwtCookie);
		response.addCookie(jwtCookie);
    }

    @DeleteMapping
    public void deleteChatRoom(@RequestBody ChatRoomRequest chatRoomRequest, HttpServletRequest request, HttpServletResponse response) {
        UUID chatRoomId = UUID.fromString(chatRoomRequest.chatRoomId());
        Cookie jwtCookie = jwtUtils.getJwtCookieFromRequest(request);
        UUID userId = UUID.fromString(jwtUtils.getUsernameFromJwtToken(jwtCookie.getValue()));
        chatRoomService.deleteChatRoom(chatRoomId, userId);
		jwtUtils.refreshJwtToken(jwtCookie);
		response.addCookie(jwtCookie);
    }

    @GetMapping
    public ChatRoomResponse getChatRoom(@RequestParam @NonNull String chatRoomId, HttpServletRequest request, HttpServletResponse response) {
        UUID _chatRoomId = UUID.fromString(chatRoomId);
        Cookie jwtCookie = jwtUtils.getJwtCookieFromRequest(request);
        UUID userId = UUID.fromString(jwtUtils.getUsernameFromJwtToken(jwtCookie.getValue()));
        ChatRoom chatRoom = chatRoomService.getChatRoom(_chatRoomId, userId);
		jwtUtils.refreshJwtToken(jwtCookie);
		response.addCookie(jwtCookie);
        return chatRoom.toResponse();
    }

    @GetMapping("/info")
    public ChatRoomInfoResponse getChatRoomInfo(@RequestParam @NonNull String chatRoomId, HttpServletRequest request, HttpServletResponse response) {
        UUID _chatRoomId = UUID.fromString(chatRoomId);
        Cookie jwtCookie = jwtUtils.getJwtCookieFromRequest(request);
        UUID userId = UUID.fromString(jwtUtils.getUsernameFromJwtToken(jwtCookie.getValue()));
        ChatRoom chatRoom = chatRoomService.getChatRoom(_chatRoomId, userId);
		jwtUtils.refreshJwtToken(jwtCookie);
		response.addCookie(jwtCookie);
        return chatRoom.toInfoResponse();
    }
}
