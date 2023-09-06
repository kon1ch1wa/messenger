package ru.shutoff.messenger;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Order;
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
	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String KEY = "key";

	@Test
	void runningContainerTest() {
		assertTrue(container.isRunning());
	}

	@Autowired
	UserInfoRepo userInfoRepo;

	@Test
	@Transactional
	public void forgotLoginTest() throws Exception {
		String invalidJson = mapper.writeValueAsString("non_existed@email.com");
		mockMvc.perform(
			post(POST_FORGOT_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJson)
		).andExpect(status().isNotFound());

		registerUser(mockMvc);

		String invalidKey = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String halfValidKey = new String(Base64.getUrlEncoder().encode(EMAIL.getBytes()));
		String key = mockMvc.perform(
			post(POST_FORGOT_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(EMAIL)
		).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		mockMvc.perform(get(GET_CHECK_LOGIN_URL).param(KEY, invalidKey)).andExpect(status().isBadRequest());
		String login = mockMvc.perform(get(GET_CHECK_LOGIN_URL).param(KEY, key)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(login, LOGIN);
		mockMvc.perform(get(GET_CHECK_LOGIN_URL).param(KEY, halfValidKey)).andExpect(status().isBadRequest());
	}

	@Test
	@Transactional
	public void forgotPasswordTest() throws Exception {
		String invalidJsonLogin = mapper.writeValueAsString("Non_existed_login");
		mockMvc.perform(
				post(POST_FORGOT_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJsonLogin)
		).andExpect(status().isNotFound());

		registerUser(mockMvc);
		String invalidJsonPass = mapper.writeValueAsString(new RestorePasswordNoAccessDto("new_test_pass_1", "new_test_pass2"));
		String newPasswordJson = mapper.writeValueAsString(new RestorePasswordNoAccessDto("new_test_pass", "new_test_pass"));

		String invalidKey = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String halfValidKey = new String(Base64.getUrlEncoder().encode(LOGIN.getBytes()));
		String key = mockMvc.perform(
				post(POST_FORGOT_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON).content(LOGIN)
		).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		mockMvc.perform(post(POST_UPDATE_PASS).param(KEY, invalidKey).content(newPasswordJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(POST_UPDATE_PASS).param(KEY, key).content(invalidJsonPass).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

		String content = mockMvc.perform(post(POST_UPDATE_PASS).param(KEY, key).content(newPasswordJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = mapper.readValue(content, User.class);
		assertNotEquals(PASS, user.getPassword());
		mockMvc.perform(post(POST_UPDATE_PASS).param(KEY, halfValidKey).content(newPasswordJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Transactional
	public void updatePasswordWithJwtTest() throws Exception {
		Cookie cookie = registerUser(mockMvc);
		String invalidJsonPass = mapper.writeValueAsString(new RestorePasswordWithAccessDto("test_pass", "new_test_pass_1", "new_test_pass2"));
		String newPasswordJson = mapper.writeValueAsString(new RestorePasswordWithAccessDto("test_pass", "new_test_pass", "new_test_pass"));

		mockMvc.perform(patch(POST_UPDATE_PASS).contentType(MediaType.APPLICATION_JSON).content(invalidJsonPass))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(patch(POST_UPDATE_PASS).contentType(MediaType.APPLICATION_JSON).content(newPasswordJson))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(patch(POST_UPDATE_PASS).contentType(MediaType.APPLICATION_JSON).content(invalidJsonPass).cookie(cookie))
				.andExpect(status().isBadRequest());
		String content = mockMvc.perform(patch(POST_UPDATE_PASS).contentType(MediaType.APPLICATION_JSON).content(newPasswordJson).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = mapper.readValue(content, User.class);
		assertNotEquals(PASS, user.getPassword());
	}
}
