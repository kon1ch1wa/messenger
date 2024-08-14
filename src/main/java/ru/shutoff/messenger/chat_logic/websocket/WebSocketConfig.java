package ru.shutoff.messenger.chat_logic.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	@Value("${spring.rabbitmq.host}")
	private String host;

	@Value("${spring.rabbitmq.stomp-port}")
	private int port;

	@Value("${spring.rabbitmq.username}")
	private String username;

	@Value("${spring.rabbitmq.password}")
	private String password;

	@Override
	public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
		registry
			.enableStompBrokerRelay("/exchange", "/queue")
			.setRelayHost(host)
			.setRelayPort(port)
			.setClientLogin(username)
			.setClientPasscode(password)
			.setSystemLogin(username)
			.setSystemPasscode(password);
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
		registry
			.addEndpoint("/websocket")
			.setAllowedOriginPatterns("*");
	}
}
