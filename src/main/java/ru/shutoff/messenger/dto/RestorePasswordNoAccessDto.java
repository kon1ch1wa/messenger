package ru.shutoff.messenger.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.annotation.Nonnull;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record RestorePasswordNoAccessDto (
		@Nonnull
		String password,
		@Nonnull
		String passwordConfirm
) {
}
