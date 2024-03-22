package ru.shutoff.messenger.chat_logic.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.chat_logic.dto.MessageDto;
import ru.shutoff.messenger.chat_logic.model.Message;
import ru.shutoff.messenger.chat_logic.service.MessageService;

@Controller
@RequiredArgsConstructor
public class MessageController {
    private final @NonNull MessageService messageService;

    @MessageMapping("/send")
    public void sendMessage(@NonNull MessageDto messageDto) {
        UUID chatRoomId = UUID.fromString(messageDto.chatRoomId());
        UUID senderId = UUID.fromString(messageDto.senderId());
        String content = messageDto.content();
        Timestamp sendDate = messageDto.sendDate();
        if (chatRoomId == null || senderId == null || content == null || sendDate == null) {
            throw new NullPointerException("Some component of MessageDto is null!");
        }
        messageService.sendMessage(chatRoomId, senderId, content, sendDate);
    }

    @GetMapping("/history")
    @ResponseBody
    public List<MessageDto> getMessageHistory(@RequestParam String chatRoomId, @RequestParam String userId) {
        UUID _chatRoomId = UUID.fromString(chatRoomId);
        UUID _userId = UUID.fromString(userId);
        List<Message> messages = messageService.getMessageHistory(_chatRoomId, _userId);
        return messages.stream().map((message) -> message.toDto()).toList();
    }
}
