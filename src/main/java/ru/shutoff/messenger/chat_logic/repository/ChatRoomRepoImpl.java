package ru.shutoff.messenger.chat_logic.repository;

import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.chat_logic.exception.ChatRoomNotFoundException;
import ru.shutoff.messenger.chat_logic.model.ChatRoom;

@Component
@RequiredArgsConstructor
public class ChatRoomRepoImpl implements ChatRoomRepo {
	private final JdbcTemplate jdbcTemplate;
	private final String SQL_SAVE_CHATROOM = "insert into chat_rooms(id, creator_id, name, description) values(?, ?, ?, ?)";
	private final String SQL_UPDATE_CHATROOM = "update chat_rooms set name=coalesce(?, name) description=coalesce(?, description) where id=?";
	private final String SQL_DELETE_CHATROOM = "delete from chat_rooms where id=?";
	private final String SQL_GET_BY_ID = "select * from chat_rooms where id=?";
	
	private final @NonNull RowMapper<ChatRoom> chatRoomMapper = (rs, rowNum) -> new ChatRoom(
		rs.getObject("id", UUID.class),
		rs.getObject("creator_id", UUID.class),
		rs.getString("name"),
		rs.getString("description")
	);

	@Override
	public void save(ChatRoom chatRoom) {
		try {
			jdbcTemplate.update(SQL_SAVE_CHATROOM, chatRoom.getChatRoomId(), chatRoom.getCreatorId(), chatRoom.getName(), chatRoom.getDescription());
		} catch (DataAccessException ex) {
			throw new DuplicateKeyException("Chat room with this id already exists");
		}
	}

	@Override
	public void update(ChatRoom chatRoom) {
		try {
			jdbcTemplate.update(SQL_UPDATE_CHATROOM, chatRoom.getChatRoomId(), chatRoom.getName(), chatRoom.getDescription());
		} catch (DataAccessException ex) {
			throw new DuplicateKeyException("Something bad happened!");
		}
	}

	@Override
	public void delete(UUID id) {
		try {
			jdbcTemplate.update(SQL_DELETE_CHATROOM, id);
		} catch (DataAccessException ex) {
			throw new DuplicateKeyException("Chat room with this id does not exists");
		}
	}

	@Override
	public ChatRoom getById(UUID id) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_BY_ID, chatRoomMapper, id);
		} catch (DataAccessException ex) {
			throw new ChatRoomNotFoundException("Chat room not found");
		}
	}
}
