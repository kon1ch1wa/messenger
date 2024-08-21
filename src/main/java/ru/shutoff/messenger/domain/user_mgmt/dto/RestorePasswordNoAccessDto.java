package ru.shutoff.messenger.domain.user_mgmt.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import jakarta.validation.constraints.NotNull;
import ru.shutoff.messenger.domain.user_mgmt.validation.PasswordConstraint;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record RestorePasswordNoAccessDto (
	@NotNull
	@PasswordConstraint
	String password,
	
	@NotNull
	@PasswordConstraint
	String passwordConfirm
) {
}
