package ru.shutoff.messenger.chat_logic.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.shutoff.messenger.chat_logic.exception.ChatRoomNotFoundException;
import ru.shutoff.messenger.chat_logic.model.ChatRoom;
import ru.shutoff.messenger.model.User;

@Component
@RequiredArgsConstructor
public class ChatRoomRepoImpl implements ChatRoomRepo {
	private final JdbcTemplate jdbcTemplate;
	private final String SQL_SAVE_CHATROOM = "insert into chat_rooms(id, first_participant_id, second_participant_id) values(?, ?, ?)";
	private final String SQL_GET_BY_ID = "select * from chat_rooms where id=?";
	private final String SQL_GET_BY_PARTICIPANTS = "select * from chat_rooms where first_participant_id=? and second_participant_id=? values(?, ?)";
	private final RowMapper<ChatRoom> chatRoomMapper = (rs, rowNum) -> new ChatRoom(
			rs.getString("id"),
			rs.getString("first_participant_id"),
			rs.getString("second_participant_id")
	);
	@Override
	public void save(ChatRoom chatRoom) {
		try {
			jdbcTemplate.update(SQL_SAVE_CHATROOM, chatRoom.getChatRoomId(), chatRoom.getFirstParticipantId(), chatRoom.getSecondParticipantId());
		} catch (DataAccessException ex) {
			throw new DuplicateKeyException("Chat room with this id already exists");
		}
	}

	@Override
	public ChatRoom getById(String id) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_BY_ID, chatRoomMapper, id);
		} catch (DataAccessException ex) {
			throw new ChatRoomNotFoundException("Chat room not found");
		}
	}

	@Override
	public ChatRoom getByParticipants(String participantAttr1, String participantAttr2) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_BY_PARTICIPANTS, chatRoomMapper, participantAttr1, participantAttr2);
		} catch (DataAccessException ex) {
			try {
				return jdbcTemplate.queryForObject(SQL_GET_BY_PARTICIPANTS, chatRoomMapper, participantAttr2, participantAttr1);
			}
			catch (DataAccessException ex1) {
				throw new ChatRoomNotFoundException("There is no chat room with such users");
			}
		}
	}
}
