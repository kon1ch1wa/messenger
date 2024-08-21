package ru.shutoff.messenger.domain.chat_logic.service;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.chat_logic.exception.ForbiddenToPerformActionException;
import ru.shutoff.messenger.domain.chat_logic.exception.NoUserInChatRoomException;
import ru.shutoff.messenger.domain.chat_logic.model.ChatRoom;
import ru.shutoff.messenger.domain.chat_logic.repository.ChatRoomRepo;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final @NonNull ChatRoomRepo chatRoomRepo;
    private final @NonNull UserAndChatRoomService userAndChatRoomService;

    public UUID addChatRoom(String name, List<UUID> users, UUID creatorId) {
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom chatRoom = ChatRoom.builder().chatRoomId(chatRoomId).name(name).creatorId(creatorId).build();
        chatRoomRepo.save(chatRoom);
        userAndChatRoomService.bindUsersToChatRoom(users, chatRoomId);
        return chatRoomId;
    }

    public ChatRoom addUserToChatRoom(UUID chatRoomId, UUID userId, UUID requesterId) {
        if (userAndChatRoomService.isUserInChatRoom(chatRoomId, userId)) {
            throw new NoUserInChatRoomException(String.format("No %s in chat room %s", userId, chatRoomId));
        }
        ChatRoom chatRoom = chatRoomRepo.getById(chatRoomId);
        if (!chatRoom.getCreatorId().equals(requesterId)) {
            throw new ForbiddenToPerformActionException(String.format("User %s can't delete chat room %s", requesterId, chatRoomId));
        }
        userAndChatRoomService.bindUserToChatRoom(userId, chatRoomId);
        return chatRoom;
    }

    public ChatRoom deleteUserFromChatRoom(UUID chatRoomId, UUID userId, UUID requesterId) {
        if (!userAndChatRoomService.isUserInChatRoom(chatRoomId, userId)) {
            throw new NoUserInChatRoomException(String.format("No %s in chat room %s", userId, chatRoomId));
        }
        ChatRoom chatRoom = chatRoomRepo.getById(chatRoomId);
        if (!chatRoom.getCreatorId().equals(requesterId)) {
            throw new ForbiddenToPerformActionException(String.format("User %s can't delete chat room %s", requesterId, chatRoomId));
        }
        if (userAndChatRoomService.getUsersByChatRoom(chatRoomId).size() == 2) {
            deleteChatRoom(chatRoomId, userId);
        } else {
            userAndChatRoomService.unbindUserFromChatRoom(userId, chatRoomId);
        }
        return chatRoom;
    }

    public void updateChatRoomInfo(UUID chatRoomId, String name, String description, UUID userId) {
        if (!userAndChatRoomService.isUserInChatRoom(chatRoomId, userId)) {
            throw new NoUserInChatRoomException(String.format("No %s in chat room %s", userId, chatRoomId));
        }
        ChatRoom chatRoom = chatRoomRepo.getById(chatRoomId);
        if (name != null) {
            chatRoom.setName(name);
        }
        if (description != null) {
            chatRoom.setDescription(description);
        }
        chatRoomRepo.update(chatRoom);
    }

    public void deleteChatRoom(UUID chatRoomId, UUID requesterId) {
        if (!userAndChatRoomService.isUserInChatRoom(chatRoomId, requesterId)) {
            throw new NoUserInChatRoomException(String.format("No %s in chat room %s", requesterId, chatRoomId));
        }
        ChatRoom chatRoom = chatRoomRepo.getById(chatRoomId);
        if (!chatRoom.getCreatorId().equals(requesterId)) {
            throw new ForbiddenToPerformActionException(String.format("User %s can't delete chat room %s", requesterId, chatRoomId));
        }
        userAndChatRoomService.deleteChatRoom(chatRoomId);
        chatRoomRepo.delete(chatRoomId);
    }

    public ChatRoom getChatRoom(UUID chatRoomId, UUID userId) {
        if (!userAndChatRoomService.isUserInChatRoom(chatRoomId, userId)) {
            throw new NoUserInChatRoomException(String.format("No %s in chat room %s", userId, chatRoomId));
        }
        return chatRoomRepo.getById(chatRoomId);
    }

    public List<UUID> getChatRoomParticipants(UUID chatRoomId) {
        return userAndChatRoomService.getUsersByChatRoom(chatRoomId);
    }
}
