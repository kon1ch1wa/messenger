package ru.shutoff.messenger.chat_logic.dto;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record ChatRoomInfoResponse(
    @NonNull
    UUID chatRoomId,

    UUID creatorId,

    List<UUID> users,

    @Nullable
    String name,

    @Nullable
    String description
) {
}
