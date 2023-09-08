package ru.shutoff.messenger.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
	@Id
	private UUID id;
	@NotNull
	private String email;
	@NotNull
	private String login;
	@NotNull
	private String password;
	@NotNull
	private boolean isActivated;
	@Nullable
	private String description;
	@Nullable
	private String phoneNumber;
	@Nullable
	private String urlTag;
	@Nullable
	private String token;
}
