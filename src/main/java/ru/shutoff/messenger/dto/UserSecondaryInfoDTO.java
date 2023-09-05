package ru.shutoff.messenger.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.annotation.Nullable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record UserSecondaryInfoDTO(
		@Nullable
		String description,
		@Nullable
		String phoneNumber,
		@Nullable
		String urlTag
) {
}
