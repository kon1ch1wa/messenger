package ru.shutoff.messenger.domain.chat_logic.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.chat_logic.model.Message;
import ru.shutoff.messenger.domain.chat_logic.repository.MessageRepo;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final @NonNull RabbitTemplate rabbitTemplate;
    private final @NonNull TopicExchange topicExchange;
    private final @NonNull MessageRepo messageRepo;

    public void sendMessage(
        @NonNull UUID chatRoomId, 
        @NonNull UUID senderId, 
        @NonNull String content, 
        @NonNull Timestamp sendDate
    ) {
        Message message = Message.builder().chatRoomId(chatRoomId).senderId(senderId).content(content).sendDate(sendDate).build();
        rabbitTemplate.convertAndSend(topicExchange.getName(), message.getChatRoomId().toString(), message.getContent());
        messageRepo.save(message);
    }

    public List<Message> getMessageHistory(UUID chatRoomId, UUID userId) {
        return messageRepo.getByChatRoomId(chatRoomId);
    }
}
