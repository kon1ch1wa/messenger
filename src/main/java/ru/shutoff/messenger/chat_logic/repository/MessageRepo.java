package ru.shutoff.messenger.chat_logic.repository;

import org.springframework.stereotype.Repository;
import ru.shutoff.messenger.chat_logic.model.Message;

import java.util.List;

@Repository
public interface MessageRepo {
	void save(Message message);
	Message getById(String id);
	List<Message> getByChatRoomId(String chatRoomId);
}
