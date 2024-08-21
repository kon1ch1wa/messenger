package ru.shutoff.messenger.domain.chat_logic.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record DeleteUserFromChatRoomRequest(
    UUID chatRoomId,
    UUID userId,
    UUID requesterId
) {
}
