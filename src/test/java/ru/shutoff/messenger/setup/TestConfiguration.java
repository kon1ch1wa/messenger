package ru.shutoff.messenger.setup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

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
}
