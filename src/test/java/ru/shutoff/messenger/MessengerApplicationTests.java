package ru.shutoff.messenger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ru.shutoff.messenger.dto.UserPrimaryInfoDTO;
import ru.shutoff.messenger.model.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(classes = MessengerApplication.class)
@AutoConfigureMockMvc
class MessengerApplicationTests {
	@Container
	private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:15.3")
			.withUsername("admin")
			.withPassword("admin")
			.withDatabaseName("messenger_db");

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", container::getJdbcUrl);
		registry.add("spring.datasource.username", container::getUsername);
		registry.add("spring.datasource.password", container::getPassword);
		registry.add("spring.liquibase.url", container::getJdbcUrl);
		registry.add("spring.liquibase.user", container::getUsername);
		registry.add("spring.liquibase.password", container::getPassword);
	}

	@Autowired
	private MockMvc mockMvc;

	private static final String EMAIL = "spring.email.receiver.daemon@gmail.com";
	private static final String LOGIN = "test_login";
	private static final String PASS = "test_pass";

	private String wrapUser(String email, String login, String password) throws JsonProcessingException {
		UserPrimaryInfoDTO info = new UserPrimaryInfoDTO(email, login, password);
		return new ObjectMapper().writeValueAsString(info);
	}

	@Test
	void runningContainerTest() {
		assertTrue(container.isRunning());
	}

	@Test
	@Transactional
	void registerUserTest() throws Exception {
		String json = wrapUser(EMAIL, LOGIN, PASS);
		String content = mockMvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = new ObjectMapper().readValue(content, User.class);
		assertEquals(user.getEmail(), EMAIL);
		assertEquals(user.getLogin(), LOGIN);
		assertEquals(user.getPassword(), PASS);
		assertFalse(user.isActivated());
	}

	@Test
	@Transactional
	void registerUserWithActivationTest() throws Exception {
		String json = wrapUser(EMAIL, LOGIN, PASS);
		String content = mockMvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = new ObjectMapper().readValue(content, User.class);
		String token = user.getToken();
		assertNotNull(token);
		mockMvc.perform(get("/user/endRegistration").param("token", UUID.randomUUID().toString()))
				.andExpect(status().isBadRequest());
		content = mockMvc.perform(get("/user/endRegistration").param("token", token))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		user = new ObjectMapper().readValue(content, User.class);
		assertTrue(user.isActivated());
	}

	@Test
	@Transactional
	void registerSameUserTest() throws Exception {
		String json = wrapUser(EMAIL, LOGIN, PASS);
		mockMvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
		mockMvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest());
	}
}
