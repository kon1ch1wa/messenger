package ru.shutoff.messenger.domain.chat_logic.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import ru.shutoff.messenger.domain.chat_logic.model.ChatRoom;

@Repository
public interface ChatRoomRepo {
	void save(ChatRoom chatRoom);

	void update(ChatRoom chatRoom);

	void delete(UUID id);

	ChatRoom getById(UUID id);
}
