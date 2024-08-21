package ru.shutoff.messenger.domain.chat_logic.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.chat_logic.exception.ChatRoomNotFoundException;
import ru.shutoff.messenger.domain.chat_logic.model.Message;

@Component
@RequiredArgsConstructor
public class MessageRepoImpl implements MessageRepo {
	private final JdbcTemplate jdbcTemplate;

	private final String SQL_SAVE_MESSAGE = "insert into messages(message_id, content, sender_id, chat_room_id, send_date) values (?, ?, ?, ?, ?)";
	private final String SQL_GET_MESSAGES_BY_CHAT_ROOM_ID = "select * from messages where chat_room_id=?";
	private final String SQL_GET_NEXTVAL = "select nextval('messages_seq')";

	private final @NonNull RowMapper<Message> messageMapper = (rs, rowNum) -> new Message(
		rs.getLong("message_id"),
		rs.getString("content"),
		rs.getObject("sender_id", UUID.class),
		rs.getObject("chat_room_id", UUID.class),
		rs.getTimestamp("send_date")
	);

	@Override
	public void save(Message message) {
		try {
			jdbcTemplate.update(SQL_SAVE_MESSAGE, message.getMessageId(), message.getContent(), message.getSenderId(), message.getChatRoomId(), message.getSendDate());
		} catch (DataAccessException ex) {
			throw new DuplicateKeyException("Message with this id already exists");
		}
	}

	@Override
	public List<Message> getByChatRoomId(UUID chatRoomId) {
		try {
			return jdbcTemplate.query(SQL_GET_MESSAGES_BY_CHAT_ROOM_ID, messageMapper, chatRoomId);
		} catch (DataAccessException ex) {
			throw new ChatRoomNotFoundException("ChatRoom is invalid");
		}
	}

	public Long getNextVal() {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_NEXTVAL, Long.class);
		} catch (DataAccessException ex) {
			throw new IllegalArgumentException("Something bad happened while getting nextval from sequence");
		}
	}
}
