package ru.shutoff.messenger.domain.chat_logic.model;

import java.util.UUID;

public record UserXChatRoom(
    Long id,
    UUID chatRoomId,
    UUID userId
) {
}
