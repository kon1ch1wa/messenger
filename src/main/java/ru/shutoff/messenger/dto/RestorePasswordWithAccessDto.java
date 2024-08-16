package ru.shutoff.messenger.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import ru.shutoff.messenger.validation.PasswordConstraint;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record RestorePasswordWithAccessDto (
		@NotNull
		@PasswordConstraint
		String oldPassword,
		@NotNull
		@PasswordConstraint
		String newPassword,
		@NotNull
		@PasswordConstraint
		String newPasswordConfirm
) {
}