package ru.shutoff.messenger.chat_logic.service;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.chat_logic.exception.NoUserInChatRoomException;
import ru.shutoff.messenger.chat_logic.model.ChatRoom;
import ru.shutoff.messenger.chat_logic.repository.ChatRoomRepo;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final @NonNull ChatRoomRepo chatRoomRepo;
    private final @NonNull UserAndChatRoomService userAndChatRoomService;

    public UUID addChatRoom(String name) {
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom chatRoom = ChatRoom.builder().chatRoomId(chatRoomId).name(name).build();
        chatRoomRepo.save(chatRoom);
        return chatRoomId;
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

    public void deleteChatRoom(UUID chatRoomId, UUID userId) {
        if (!userAndChatRoomService.isUserInChatRoom(chatRoomId, userId)) {
            throw new NoUserInChatRoomException(String.format("No %s in chat room %s", userId, chatRoomId));
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
}
