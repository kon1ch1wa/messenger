package ru.shutoff.messenger.domain.chat_logic.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public interface UserAndChatRoomRepo {
    void bindUserToChatRoom(UUID userId, UUID chatRoomId);
    void bindUsersToChatRoom(List<UUID> userIds, UUID chatRoomId);
    void unbindUserFromChatRoom(UUID userId, UUID chatRoomId);
    void unbindUsersFromChatRoom(List<UUID> userIds, UUID chatRoomId);
    void deleteChatRoom(UUID chatRoomId);
    List<UUID> getUsersByChatRoom(UUID chatRoomId);
}
