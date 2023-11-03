package ru.shutoff.messenger.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ru.shutoff.messenger.MessengerApplication;
import ru.shutoff.messenger.dto.LoginRequest;
import ru.shutoff.messenger.dto.UserSecondaryInfoDTO;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.setup.SetupMethods;
import ru.shutoff.messenger.setup.TestConfiguration;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(classes = MessengerApplication.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc
class RegistrationAndUpdatingIntegrationTests {
	@Container
	private static final PostgreSQLContainer<?> container = SetupMethods.container;

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

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private String jwtCookieName;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void runningContainerTest() {
		assertTrue(container.isRunning());
	}

	@Test
	void registerUserTest() throws Exception {
		String json = SetupMethods.wrapPrimaryInfo();
		String content = mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = mapper.readValue(content, User.class);
		assertEquals(SetupMethods.EMAIL, user.getEmail());
		assertEquals(SetupMethods.LOGIN, user.getLogin());
		assertFalse(user.isActivated());
	}

	@Test
	void registerUserWithActivationTest() throws Exception {
		String json = SetupMethods.wrapPrimaryInfo();
		String content = mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = mapper.readValue(content, User.class);
		String token = user.getToken();
		assertNotNull(token);
		mockMvc.perform(get(SetupMethods.AUTH_API_USER_URL).param("token", UUID.randomUUID().toString()))
				.andExpect(status().isBadRequest());
		content = mockMvc.perform(get(SetupMethods.AUTH_API_USER_URL).param("token", token))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		user = mapper.readValue(content, User.class);
		assertTrue(user.isActivated());
	}

	@Test
	void registerUserAndLoginWithoutActivation() throws Exception {
		String json = SetupMethods.wrapPrimaryInfo();
		String content = mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = mapper.readValue(content, User.class);
		String loginJson = mapper.writeValueAsString(new LoginRequest(user.getLogin(), user.getPassword()));
		mockMvc.perform(post(SetupMethods.AUTH_API_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(loginJson))
				.andExpect(status().isUnauthorized());
		String token = user.getToken();
		mockMvc.perform(get(SetupMethods.AUTH_API_USER_URL).param("token", token))
				.andExpect(status().isOk());
		Cookie cookie = mockMvc.perform(post(SetupMethods.AUTH_API_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(loginJson))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		assertNotNull(cookie);
	}

	@Test
	void registerSameUserTest() throws Exception {
		String json1 = SetupMethods.wrapPrimaryInfo();
		String json2 = SetupMethods.wrapPrimaryInfo(SetupMethods.EMAIL, "test_another_login", "Vitalya", SetupMethods.PASS);
		String json3 = SetupMethods.wrapPrimaryInfo("test.another@dev.ru", SetupMethods.LOGIN, "Vitalya", SetupMethods.PASS);
		mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(json1))
				.andExpect(status().isOk());
		mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(json2))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(json3))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateUserUnauthorizedTest() throws Exception {
		String jsonPrimary = SetupMethods.wrapPrimaryInfo();
		mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(jsonPrimary))
				.andExpect(status().isOk());
		String jsonSecondary = SetupMethods.wrapSecondaryInfo();
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(jsonSecondary))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void updateUserTest() throws Exception {
		MockHttpServletResponse response;
		User user;
		Cookie cookie = SetupMethods.activateUser(mockMvc, SetupMethods.registerUser(mockMvc));
		String jsonSecondary_d = SetupMethods.wrapSecondaryInfo(SetupMethods.DESC, null, null);
		response = mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(jsonSecondary_d).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse();
		user = mapper.readValue(response.getContentAsString(), User.class);
		assertEquals(SetupMethods.DESC, user.getDescription());
		assertNull(user.getPhoneNumber());
		assertNull(user.getUrlTag());

		String jsonSecondary_p = SetupMethods.wrapSecondaryInfo(null, SetupMethods.PHONE_NUMBER, null);
		response = mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(jsonSecondary_p).cookie(response.getCookie(jwtCookieName)))
				.andExpect(status().isOk()).andReturn().getResponse();
		user = mapper.readValue(response.getContentAsString(), User.class);
		assertEquals(SetupMethods.DESC, user.getDescription());
		assertEquals(SetupMethods.PHONE_NUMBER, user.getPhoneNumber());
		assertNull(user.getUrlTag());

		String jsonSecondary_u = SetupMethods.wrapSecondaryInfo(null, null, SetupMethods.URL_TAG);
		response = mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(jsonSecondary_u).cookie(response.getCookie(jwtCookieName)))
				.andExpect(status().isOk()).andReturn().getResponse();
		user = mapper.readValue(response.getContentAsString(), User.class);
		assertEquals(SetupMethods.DESC, user.getDescription());
		assertEquals(SetupMethods.PHONE_NUMBER, user.getPhoneNumber());
		assertEquals(SetupMethods.URL_TAG, user.getUrlTag());
		assertNotNull(response.getCookie(jwtCookieName));
	}

	@Test
	public void updateDuplicatedDataTest() throws Exception {
		String phoneNumberJson = mapper.writeValueAsString(new UserSecondaryInfoDTO(null, "+79217642904", null));
		String urlTagJson = mapper.writeValueAsString(new UserSecondaryInfoDTO(null, null, "kon1ch1wa"));
		Cookie cookie1 = SetupMethods.activateUser(mockMvc, SetupMethods.registerUser(mockMvc));
		Cookie cookie2 = SetupMethods.activateUser(mockMvc, SetupMethods.registerAnotherUser(mockMvc));

		cookie1 = mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie1).contentType(MediaType.APPLICATION_JSON).content(phoneNumberJson))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		cookie2 = mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie2).contentType(MediaType.APPLICATION_JSON).content(urlTagJson))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie1).contentType(MediaType.APPLICATION_JSON).content(urlTagJson))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie2).contentType(MediaType.APPLICATION_JSON).content(phoneNumberJson))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie1).contentType(MediaType.APPLICATION_JSON).content(phoneNumberJson))
				.andExpect(status().isOk());
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie2).contentType(MediaType.APPLICATION_JSON).content(urlTagJson))
				.andExpect(status().isOk());
	}

	@AfterEach
	void cleanUpTable() {
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_data");
	}
}