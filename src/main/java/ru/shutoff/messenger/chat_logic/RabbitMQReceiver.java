package ru.shutoff.messenger.chat_logic;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.shutoff.messenger.chat_logic.dto.MessageDto;

@Component
@RabbitListener(queues = "rabbitmq.queue", id = "listener")
public class RabbitMQReceiver {
	@RabbitHandler
	public void receive(MessageDto message) {
		System.out.println(message);
	}
}
