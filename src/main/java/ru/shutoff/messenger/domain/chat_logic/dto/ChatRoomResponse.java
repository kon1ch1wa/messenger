package ru.shutoff.messenger.domain.chat_logic.dto;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record ChatRoomResponse(
    @NonNull
    UUID chatRoomId,

    @NonNull
    UUID creatorId,

    @NonNull
    List<UUID> users,

    @NonNull
    String name
) {
}
