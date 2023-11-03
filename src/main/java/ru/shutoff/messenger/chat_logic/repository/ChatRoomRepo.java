package ru.shutoff.messenger.chat_logic.repository;

import org.springframework.stereotype.Repository;
import ru.shutoff.messenger.chat_logic.model.ChatRoom;

@Repository
public interface ChatRoomRepo {
	void save(ChatRoom chatRoom);

	ChatRoom getById(String id);
	ChatRoom getByParticipants(String participantAttribute1, String participantAttribute2);
}
