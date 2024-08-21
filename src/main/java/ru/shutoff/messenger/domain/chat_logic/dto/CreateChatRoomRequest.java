package ru.shutoff.messenger.domain.chat_logic.dto;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record CreateChatRoomRequest(
    @NonNull
    String name,

    @NonNull
    List<String> users,

    @NonNull
    UUID creatorId
) {
}
