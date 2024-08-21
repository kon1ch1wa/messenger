package ru.shutoff.messenger.domain.file_handling.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record FileResponse(
    UUID fileId
) {
}
