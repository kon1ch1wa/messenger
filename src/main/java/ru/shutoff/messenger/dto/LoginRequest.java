package ru.shutoff.messenger.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record LoginRequest (
		String login,
		String password
) {
}
