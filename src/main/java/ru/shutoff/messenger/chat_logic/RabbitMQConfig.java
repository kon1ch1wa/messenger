package ru.shutoff.messenger.chat_logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMQConfig {
	private final String queueName = "rabbitmq.queue";
	private final String exchange = "rabbitmq.exchange";
	private final String routingKey = "rabbitmq.routing_key";
	private final String username = "RMQAdmin";
	private final String password = "RMQPassword";
	private final String host = "rabbit";
	private final Integer concurrentConsumers = 1;
	private final Integer maxConcurrentConsumers = 100;

	@Bean
	public Queue queue() {
		return new Queue(queueName, false);
	}

	@Bean
	public DirectExchange exchange() {
		return new DirectExchange(exchange);
	}

	@Bean
	public Binding binding(Queue queue, DirectExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		ObjectMapper objectMapper = new ObjectMapper();
		return new Jackson2JsonMessageConverter(objectMapper);
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost(host);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		return connectionFactory;
	}

	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}

	//@Bean
	public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setDefaultReceiveQueue(queueName);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		rabbitTemplate.setReplyAddress(queue().getName());
		rabbitTemplate.setUseDirectReplyToContainer(false);
		return rabbitTemplate;
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
		final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
		factory.setMessageConverter(jsonMessageConverter());
		factory.setConcurrentConsumers(concurrentConsumers);
		factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
		return factory;
	}
}
