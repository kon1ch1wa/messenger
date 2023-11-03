package ru.shutoff.messenger.chat_logic.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final CustomHandshakeHandler customHandshakeHandler;
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		/*registry
				.enableStompBrokerRelay("/topic", "/queue/messages")
				.setRelayHost("rabbit")
				.setRelayPort(5672)
				.setClientLogin("RMQAdmin")
				.setClientPasscode("RMQPassword");*/
		registry.enableSimpleBroker("/topic", "/queue/messages");
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-endpoint")
				//.setHandshakeHandler(customHandshakeHandler)
				.withSockJS();
	}
}
