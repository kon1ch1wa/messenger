package ru.shutoff.messenger.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class User {
	private UUID id;
	private String email;
	private String login;
	private String name;
	private String password;
	private boolean isActivated;
	private String description;
	private String phoneNumber;
	private String urlTag;
	private String token;
}
