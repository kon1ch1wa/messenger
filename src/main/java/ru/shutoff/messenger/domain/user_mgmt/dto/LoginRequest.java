package ru.shutoff.messenger.domain.user_mgmt.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import jakarta.validation.constraints.NotNull;
import ru.shutoff.messenger.domain.user_mgmt.validation.LoginConstraint;
import ru.shutoff.messenger.domain.user_mgmt.validation.PasswordConstraint;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record LoginRequest (
	@NotNull
	@LoginConstraint
	String login,
	
	@NotNull
	@PasswordConstraint
	String password
) {
}
