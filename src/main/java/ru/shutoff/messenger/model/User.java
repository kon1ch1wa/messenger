package ru.shutoff.messenger.model;

import jakarta.annotation.Nullable;
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
	private String email;
	private String login;
	private String password;
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
