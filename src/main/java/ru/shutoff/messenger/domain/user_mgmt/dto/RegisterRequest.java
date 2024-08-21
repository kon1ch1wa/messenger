package ru.shutoff.messenger.domain.user_mgmt.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import jakarta.validation.constraints.Email;
import ru.shutoff.messenger.domain.user_mgmt.validation.LoginConstraint;
import ru.shutoff.messenger.domain.user_mgmt.validation.PasswordConstraint;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record RegisterRequest (
	@Email
	String email,

	@LoginConstraint
	String login,

	@LoginConstraint
	String name,

	@PasswordConstraint
	String password
) {
}
