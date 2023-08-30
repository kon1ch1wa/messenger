package ru.shutoff.messenger.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.validation.constraints.NotNull;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record UserPrimaryInfoDTO (
		@NotNull
		String email,
		@NotNull
		String login,
		@NotNull
		String password
) {
}
