package ru.shutoff.messenger.domain.user_mgmt.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import jakarta.annotation.Nullable;
import ru.shutoff.messenger.domain.user_mgmt.validation.PhoneNumberConstraint;
import ru.shutoff.messenger.domain.user_mgmt.validation.UrlTagConstraint;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record UpdateInfoRequest(
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
