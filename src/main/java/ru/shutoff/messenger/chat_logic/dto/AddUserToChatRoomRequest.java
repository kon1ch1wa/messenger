package ru.shutoff.messenger.chat_logic.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record AddUserToChatRoomRequest(
    UUID chatRoomId,
    UUID userId,
    UUID requesterId
) {
}
