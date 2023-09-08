package ru.shutoff.messenger.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ru.shutoff.messenger.MessengerApplication;
import ru.shutoff.messenger.dto.RestorePasswordNoAccessDto;
import ru.shutoff.messenger.dto.RestorePasswordWithAccessDto;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.repository.UserInfoRepo;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.shutoff.messenger.setup.SetupMethods.*;

@Testcontainers
@SpringBootTest(classes = MessengerApplication.class)
@AutoConfigureMockMvc
public class ForgotAndUpdateCredsIntegrationTests {
	@Container
	private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:15.3")
			.withUsername("admin")
			.withPassword("admin")
			.withDatabaseName("messenger_db")
			.withExposedPorts(5432);

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", container::getJdbcUrl);
		registry.add("spring.datasource.username", container::getUsername);
		registry.add("spring.datasource.password", container::getPassword);
		registry.add("spring.liquibase.url", container::getJdbcUrl);
		registry.add("spring.liquibase.user", container::getUsername);
		registry.add("spring.liquibase.password", container::getPassword);
		container.start();
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String KEY = "key";

	@Test
	void runningContainerTest() {
		assertTrue(container.isRunning());
	}

	@Test
	public void forgotLoginTest() throws Exception {
		String invalidJson = mapper.writeValueAsString("non_existed@email.com");
		mockMvc.perform(
			post(FORGOT_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJson)
		).andExpect(status().isNotFound());

		registerUser(mockMvc);

		String invalidKey = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String halfValidKey = new String(Base64.getUrlEncoder().encode(EMAIL.getBytes()));
		String key = mockMvc.perform(
			post(FORGOT_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(EMAIL)
		).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		mockMvc.perform(get(CHECK_LOGIN_URL).param(KEY, invalidKey)).andExpect(status().isBadRequest());
		String login = mockMvc.perform(get(CHECK_LOGIN_URL).param(KEY, key)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(login, LOGIN);
		mockMvc.perform(get(CHECK_LOGIN_URL).param(KEY, halfValidKey)).andExpect(status().isBadRequest());
	}

	@Test
	public void forgotPasswordTest() throws Exception {
		String invalidJsonLogin = mapper.writeValueAsString("Non_existed_login");
		mockMvc.perform(
				post(FORGOT_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJsonLogin)
		).andExpect(status().isNotFound());

		registerUser(mockMvc);
		String invalidJsonPass = mapper.writeValueAsString(new RestorePasswordNoAccessDto("New_Test_Pass_1", "New_Test_Pass_2"));
		String newPasswordJson = mapper.writeValueAsString(new RestorePasswordNoAccessDto("New_Test_Pass_0", "New_Test_Pass_0"));

		String invalidKey = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String halfValidKey = new String(Base64.getUrlEncoder().encode(LOGIN.getBytes()));
		String key = mockMvc.perform(
				post(FORGOT_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(LOGIN)
		).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		mockMvc.perform(post(UPDATE_PASSWORD_URL).param(KEY, invalidKey).content(newPasswordJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(UPDATE_PASSWORD_URL).param(KEY, key).content(invalidJsonPass).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

		String content = mockMvc.perform(post(UPDATE_PASSWORD_URL).param(KEY, key).content(newPasswordJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = mapper.readValue(content, User.class);
		assertNotEquals(PASS, user.getPassword());
		mockMvc.perform(post(UPDATE_PASSWORD_URL).param(KEY, halfValidKey).content(newPasswordJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void updatePasswordWithJwtTest() throws Exception {
		Cookie cookie = registerUser(mockMvc);
		String invalidJsonPass = mapper.writeValueAsString(new RestorePasswordWithAccessDto("Test_Pass_0", "New_Test_Pass_1", "New_Test_Pass_2"));
		String newPasswordJson = mapper.writeValueAsString(new RestorePasswordWithAccessDto("Test_Pass_0", "New_Test_Pass_0", "New_Test_Pass_0"));

		mockMvc.perform(patch(UPDATE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJsonPass))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(patch(UPDATE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(newPasswordJson))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(patch(UPDATE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJsonPass).cookie(cookie))
				.andExpect(status().isBadRequest());
		String content = mockMvc.perform(patch(UPDATE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(newPasswordJson).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = mapper.readValue(content, User.class);
		assertNotEquals(PASS, user.getPassword());
	}

	@AfterEach
	void cleanUpTable() {
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_data");
	}
}
