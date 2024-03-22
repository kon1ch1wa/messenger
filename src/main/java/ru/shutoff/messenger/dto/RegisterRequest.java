package ru.shutoff.messenger.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import jakarta.validation.constraints.Email;
import ru.shutoff.messenger.validation.LoginConstraint;
import ru.shutoff.messenger.validation.PasswordConstraint;

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
