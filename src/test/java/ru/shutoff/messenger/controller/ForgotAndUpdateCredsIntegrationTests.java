package ru.shutoff.messenger.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.shutoff.messenger.setup.SetupMethods.CHECK_LOGIN_URL;
import static ru.shutoff.messenger.setup.SetupMethods.EMAIL;
import static ru.shutoff.messenger.setup.SetupMethods.FORGOT_LOGIN_URL;
import static ru.shutoff.messenger.setup.SetupMethods.FORGOT_PASSWORD_URL;
import static ru.shutoff.messenger.setup.SetupMethods.LOGIN;
import static ru.shutoff.messenger.setup.SetupMethods.PASS;
import static ru.shutoff.messenger.setup.SetupMethods.UPDATE_PASSWORD_URL;
import static ru.shutoff.messenger.setup.SetupMethods.activateUser;
import static ru.shutoff.messenger.setup.SetupMethods.registerUser;

import java.util.Base64;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import ru.shutoff.messenger.MessengerApplication;
import ru.shutoff.messenger.dto.RestorePasswordNoAccessDto;
import ru.shutoff.messenger.dto.RestorePasswordWithAccessDto;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.setup.SetupMethods;
import ru.shutoff.messenger.setup.TestConfiguration;

@Testcontainers
@SpringBootTest(classes = MessengerApplication.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc
@Slf4j
public class ForgotAndUpdateCredsIntegrationTests {
	public static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(SetupMethods.postgresImageName)
			.withUsername("admin")
			.withPassword("admin")
			.withDatabaseName("messenger_db");

	@MockBean
	private RabbitTemplate rabbitTemplate;

	@MockBean
	private RabbitAdmin rabbitAdmin;

	@BeforeAll
	static void beforeAll() {
		postgresContainer.start();
	}

	@AfterAll
	static void afterAll() {
		postgresContainer.stop();
		postgresContainer.close();
	}

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresContainer::getUsername);
		registry.add("spring.datasource.password", postgresContainer::getPassword);
		registry.add("spring.liquibase.url", postgresContainer::getJdbcUrl);
		registry.add("spring.liquibase.user", postgresContainer::getUsername);
		registry.add("spring.liquibase.password", postgresContainer::getPassword);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ObjectMapper mapper;

	private static final String KEY = "key";

	@Test
	void runningContainerTest() {
		assertTrue(postgresContainer.isRunning());
	}

	@Test
	public void forgotLoginTest() throws Exception {
		String invalidJson = mapper.writeValueAsString("non_existed@email.com");
		mockMvc.perform(
			post(FORGOT_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJson)
		).andExpect(status().isNotFound());

		activateUser(mockMvc, registerUser(mockMvc));

		String invalidKey = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String halfValidKey = new String(Base64.getUrlEncoder().encode(EMAIL.getBytes()));
		String key = mockMvc.perform(
			post(FORGOT_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(EMAIL)
		).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		mockMvc.perform(get(CHECK_LOGIN_URL).param(KEY, invalidKey)).andExpect(status().isBadRequest());
		String login = mockMvc.perform(get(CHECK_LOGIN_URL).param(KEY, key)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(LOGIN, login);
		mockMvc.perform(get(CHECK_LOGIN_URL).param(KEY, halfValidKey)).andExpect(status().isBadRequest());
	}

	@Test
	public void forgotPasswordTest() throws Exception {
		String invalidJsonLogin = mapper.writeValueAsString("Non_existed_login");
		mockMvc.perform(
				post(FORGOT_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJsonLogin)
		).andExpect(status().isNotFound());

		activateUser(mockMvc, registerUser(mockMvc));
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
		Cookie cookie = activateUser(mockMvc, registerUser(mockMvc));
		String invalidJsonPass = mapper.writeValueAsString(new RestorePasswordWithAccessDto("Test_Pass_0", "New_Test_Pass_1", "New_Test_Pass_2"));
		String newPasswordJson = mapper.writeValueAsString(new RestorePasswordWithAccessDto("Test_Pass_0", "New_Test_Pass_0", "New_Test_Pass_0"));
		String oldPasswordInvalidJson = mapper.writeValueAsString(new RestorePasswordWithAccessDto("Test_Pass_1", "New_Test_Pass_0", "New_Test_Pass_0"));

		mockMvc.perform(patch(UPDATE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJsonPass))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(patch(UPDATE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(newPasswordJson))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(patch(UPDATE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(oldPasswordInvalidJson))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(patch(UPDATE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJsonPass).cookie(cookie))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(UPDATE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(oldPasswordInvalidJson).cookie(cookie))
				.andExpect(status().isBadRequest());
		String content = mockMvc.perform(patch(UPDATE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(newPasswordJson).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = mapper.readValue(content, User.class);
		assertNotEquals(PASS, user.getPassword());
	}

	@AfterEach
	void cleanUpTable() {
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
	}
}
