package ru.shutoff.messenger.chat_logic.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.shutoff.messenger.chat_logic.exception.ChatRoomNotFoundException;
import ru.shutoff.messenger.chat_logic.exception.MessageNotFoundException;
import ru.shutoff.messenger.chat_logic.model.ChatRoom;
import ru.shutoff.messenger.chat_logic.model.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageRepoImpl implements MessageRepo {
	private final JdbcTemplate jdbcTemplate;

	private final String SQL_SAVE_MESSAGE = "insert into messages(message_id, content, sender_id, receiver_id, chat_room_id, send_date) values (?, ?, ?, ?, ?, ?)";
	private final String SQL_GET_MESSAGE_BY_ID = "select * from messages where message_id=?";
	private final String SQL_GET_MESSAGES_BY_CHAT_ROOM_ID = "select * from messages where chat_room_id=?";
	private final RowMapper<Message> messageMapper = (rs, rowNum) -> new Message(
			rs.getString("message_id"),
			rs.getString("content"),
			rs.getString("sender_id"),
			rs.getString("receiver_id"),
			rs.getString("chat_room_id"),
			rs.getDate("send_date")
	);
	@Override
	public void save(Message message) {
		try {
			jdbcTemplate.update(SQL_SAVE_MESSAGE, message.getMessageId(), message.getContent(), message.getSenderId(), message.getReceiverId(), message.getChatRoomId(), message.getSendDate());
		} catch (DataAccessException ex) {
			throw new DuplicateKeyException("Message with this id already exists");
		}
	}

	@Override
	public Message getById(String id) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_MESSAGE_BY_ID, messageMapper, id);
		} catch (DataAccessException ex) {
			throw new MessageNotFoundException("Message not found");
		}
	}

	@Override
	public List<Message> getByChatRoomId(String chatRoomId) {
		try {
			return jdbcTemplate.query(SQL_GET_MESSAGES_BY_CHAT_ROOM_ID, messageMapper, chatRoomId);
		} catch (DataAccessException ex) {
			throw new ChatRoomNotFoundException("ChatRoom is invalid");
		}
	}
}
