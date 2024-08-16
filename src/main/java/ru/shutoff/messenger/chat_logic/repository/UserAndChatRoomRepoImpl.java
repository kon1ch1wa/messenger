package ru.shutoff.messenger.chat_logic.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.chat_logic.exception.UserAndChatRoomException;

@Component
@RequiredArgsConstructor
public class UserAndChatRoomRepoImpl implements UserAndChatRoomRepo {
	private final JdbcTemplate jdbcTemplate;
    private final String SQL_BIND_USER_TO_CHATROOM = "INSERT INTO user_and_chat_room (user_id, chat_room_id) VALUES (?, ?)";
    private final String SQL_BIND_USERS_TO_CHATROOM = "INSERT INTO user_and_chat_room (user_id, chat_room_id) VALUES (?, ?)";
    private final String SQL_UNBIND_USER_FROM_CHATROOM = "DELETE FROM user_and_chat_room WHERE user_id = ? AND chat_room_id = ?";
    private final String SQL_UNBIND_USERS_FROM_CHATROOM = "DELETE FROM user_and_chat_room WHERE user_id IN (?) AND chat_room_id = ?";
    private final String SQL_DELETE_CHATROOM = "DELETE FROM user_and_chat_room WHERE chat_room_id = ?";
    private final String SQL_GET_USERS_BY_CHATROOM = "SELECT user_id FROM user_and_chat_room WHERE chat_room_id = ?";

    @Override
    public void bindUserToChatRoom(UUID userId, UUID chatRoomId) {
		try {
			jdbcTemplate.update(SQL_BIND_USER_TO_CHATROOM, userId, chatRoomId);
		} catch (DataAccessException ex) {
			throw new UserAndChatRoomException(ex.getMessage());
		}
    }

    @Override
    public void bindUsersToChatRoom(List<UUID> userIds, UUID chatRoomId) {
		try {
            for (UUID userId: userIds) {
                jdbcTemplate.update(SQL_BIND_USERS_TO_CHATROOM, userId, chatRoomId);
            }
		} catch (DataAccessException ex) {
			throw new UserAndChatRoomException(ex.getMessage());
		}
    }

    @Override
    public void unbindUserFromChatRoom(UUID userId, UUID chatRoomId) {
		try {
			jdbcTemplate.update(SQL_UNBIND_USER_FROM_CHATROOM, chatRoomId, userId);
		} catch (DataAccessException ex) {
			throw new UserAndChatRoomException(ex.getMessage());
		}
    }

    @Override
    public void unbindUsersFromChatRoom(List<UUID> userIds, UUID chatRoomId) {
		try {
			jdbcTemplate.update(SQL_UNBIND_USERS_FROM_CHATROOM, chatRoomId, userIds);
		} catch (DataAccessException ex) {
			throw new UserAndChatRoomException(ex.getMessage());
		}
    }

    @Override
    public void deleteChatRoom(UUID chatRoomId) {
		try {
			jdbcTemplate.update(SQL_DELETE_CHATROOM, chatRoomId);
		} catch (DataAccessException ex) {
			throw new UserAndChatRoomException(ex.getMessage());
		}
    }

    @Override
    public List<UUID> getUsersByChatRoom(UUID chatRoomId) {
		try {
			return jdbcTemplate.queryForList(SQL_GET_USERS_BY_CHATROOM, UUID.class, chatRoomId);
		} catch (DataAccessException ex) {
			throw new UserAndChatRoomException(ex.getMessage());
		}
    }
}
