package ru.shutoff.messenger.chat_logic.service;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.chat_logic.repository.UserAndChatRoomRepo;

@Service
@RequiredArgsConstructor
public class UserAndChatRoomService {
    private final @NonNull UserAndChatRoomRepo userAndChatRoomRepo;

    public void bindUsersToChatRoom(List<UUID> userIds, UUID chatRoomId) {
        userAndChatRoomRepo.bindUsersToChatRoom(userIds, chatRoomId);
    }

    public void bindUserToChatRoom(UUID userId, UUID chatRoomId) {
        userAndChatRoomRepo.bindUserToChatRoom(userId, chatRoomId);
    }

    public void unbindUserFromChatRoom(UUID userId, UUID chatRoomId) {
        userAndChatRoomRepo.unbindUserFromChatRoom(userId, chatRoomId);
    }

    public List<UUID> getUsersByChatRoom(UUID chatRoomId) {
        return userAndChatRoomRepo.getUsersByChatRoom(chatRoomId);
    }

    public void deleteChatRoom(UUID chatRoomId) {
        userAndChatRoomRepo.deleteChatRoom(chatRoomId);
    }

    public boolean isUserInChatRoom(UUID chatRoomId, UUID userId) {
        List<UUID> userIds = userAndChatRoomRepo.getUsersByChatRoom(chatRoomId);
        return userIds.contains(userId);
    }
}
