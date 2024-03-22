package ru.shutoff.messenger.chat_logic.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import ru.shutoff.messenger.chat_logic.model.Message;

@Repository
public interface MessageRepo {
	void save(Message message);
	List<Message> getByChatRoomId(UUID chatRoomId);
}
