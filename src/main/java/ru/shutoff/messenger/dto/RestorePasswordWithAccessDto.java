package ru.shutoff.messenger.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.annotation.Nonnull;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record RestorePasswordWithAccessDto (
		@Nonnull
		String oldPassword,
		@Nonnull
		String newPassword,
		@Nonnull
		String newPasswordConfirm
) {
}
