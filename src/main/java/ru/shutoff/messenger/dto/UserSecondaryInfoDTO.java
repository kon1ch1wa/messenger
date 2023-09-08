package ru.shutoff.messenger.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.annotation.Nullable;
import ru.shutoff.messenger.validation.PhoneNumberConstraint;
import ru.shutoff.messenger.validation.UrlTagConstraint;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record UserSecondaryInfoDTO(
		@Nullable
		String description,
		@Nullable
		@PhoneNumberConstraint
		String phoneNumber,
		@Nullable
		@UrlTagConstraint
		String urlTag
) {
}
