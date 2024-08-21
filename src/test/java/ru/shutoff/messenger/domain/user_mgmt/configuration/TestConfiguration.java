package ru.shutoff.messenger.domain.user_mgmt.configuration;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import io.minio.MinioClient;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {
	@Value("${jwt.cookie_name}")
	private String JWT_COOKIE_NAME;
	@Bean
	public String jwtCookieName() {
		return JWT_COOKIE_NAME;
	}
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@MockBean
	public MinioClient minioClient;

	@MockBean
	private RabbitTemplate rabbitTemplate;

	@MockBean
	private RabbitAdmin rabbitAdmin;
}
