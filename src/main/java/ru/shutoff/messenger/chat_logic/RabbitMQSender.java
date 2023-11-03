package ru.shutoff.messenger.chat_logic;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;
import ru.shutoff.messenger.chat_logic.dto.MessageDto;

@Service
@RequiredArgsConstructor
public class RabbitMQSender {
	private final AmqpTemplate rabbitTemplate;
	private final Queue queue;

	public void send(MessageDto message) {
		rabbitTemplate.convertAndSend(queue.getName(), message);
	}
}
