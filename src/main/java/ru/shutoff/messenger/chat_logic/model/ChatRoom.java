package ru.shutoff.messenger.chat_logic.model;

import java.util.UUID;

import io.micrometer.common.lang.NonNull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatRoom {
	@NotNull
	private UUID chatRoomId;

	@NotNull
	private UUID creatorId;

	@NonNull
	private String name;

	@Nullable
	private String description;
}
