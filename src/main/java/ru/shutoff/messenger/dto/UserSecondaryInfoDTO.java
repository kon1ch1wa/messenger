package ru.shutoff.messenger.dto;

import jakarta.annotation.Nullable;

public record UserSecondaryInfoDTO(
		@Nullable
		String description,
		@Nullable
		String phoneNumber,
		@Nullable
		String urlTag
) {
}
