package ru.shutoff.messenger.domain.chat_logic.dto;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record ChatRoomInfoRequest(
    @NonNull
    String chatRoomId,

    @Nullable
    String name,

    @Nullable
    String description
) {
}
