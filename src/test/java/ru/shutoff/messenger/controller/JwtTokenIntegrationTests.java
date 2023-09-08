package ru.shutoff.messenger.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ru.shutoff.messenger.MessengerApplication;
import ru.shutoff.messenger.dto.LoginRequest;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static ru.shutoff.messenger.setup.SetupMethods.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = MessengerApplication.class)
@AutoConfigureMockMvc
public class JwtTokenIntegrationTests {
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
		registry.add("jwt.expiration_time", () -> "2000");
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	void runningContainerTest() {
		assertTrue(container.isRunning());
	}

	@Test
	void pingUnauthorizedNotCreatingCookieTest() throws Exception {
		Optional<Cookie> cookie = Optional.ofNullable(mockMvc.perform(get(PING_URL))
				.andExpect(status().isUnauthorized()).andReturn().getResponse().getCookie(JWT_COOKIE_NAME));
		assertFalse(cookie.isPresent());
	}

	@Test
	void pingAuthorizedCookieTest() throws Exception {
		Cookie cookie = registerUser(mockMvc);
		mockMvc.perform(get(PING_URL).cookie(cookie)).andExpect(status().isOk());
	}

	@Test
	void pingUpdatingCookieTest() throws Exception {
		mockMvc.perform(get(PING_URL)).andExpect(status().isUnauthorized());
		Cookie cookie = registerUser(mockMvc);
		Optional<Cookie> cookieNew = Optional.ofNullable(mockMvc.perform(get(PING_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(JWT_COOKIE_NAME));
		assertTrue(cookieNew.isPresent());
		mockMvc.perform(get(PING_URL).cookie(cookieNew.get())).andExpect(status().isOk());
	}

	@Test
	void pingUpdatingCookieLifecycleTest() throws Exception {
		Cookie cookie = registerUser(mockMvc);

		cookie = mockMvc.perform(get(PING_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(JWT_COOKIE_NAME);
		Thread.sleep(TimeUnit.SECONDS.toMillis(1));
		cookie = mockMvc.perform(get(PING_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(JWT_COOKIE_NAME);
		Thread.sleep(TimeUnit.SECONDS.toMillis(2));
		mockMvc.perform(get(PING_URL)).andExpect(status().isUnauthorized());
	}

	@Test
	void loginTest() throws Exception {
		mockMvc.perform(get(AUTH_API_LOGOUT_URL)).andExpect(status().isUnauthorized());
		Cookie cookie = registerUser(mockMvc);

		cookie = mockMvc.perform(get(PING_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(JWT_COOKIE_NAME);
		cookie = mockMvc.perform(get(AUTH_API_LOGOUT_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(JWT_COOKIE_NAME);
		mockMvc.perform(get(PING_URL)).andExpect(status().isUnauthorized());

		String authJson = mapper.writeValueAsString(new LoginRequest(LOGIN, PASS));
		Cookie authCookie = mockMvc.perform(post(AUTH_API_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(authJson))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(JWT_COOKIE_NAME);
		authCookie = mockMvc.perform(get(PING_URL).cookie(authCookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(JWT_COOKIE_NAME);
		mockMvc.perform(get(AUTH_API_LOGOUT_URL).cookie(authCookie)).andExpect(status().isOk());

		Optional<Cookie> authCookieNew = Optional.ofNullable(mockMvc.perform(post(AUTH_API_LOGIN_URL))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getCookie(JWT_COOKIE_NAME));
		assertFalse(authCookieNew.isPresent());
		String authJson1 = mapper.writeValueAsString(new LoginRequest(LOGIN, "Wrong_Password_0"));
		mockMvc.perform(post(AUTH_API_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(authJson1))
				.andExpect(status().isUnauthorized());
		String authJson2 = mapper.writeValueAsString(new LoginRequest("Non_existed_login", PASS));
		mockMvc.perform(post(AUTH_API_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(authJson2))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void logoutTest() throws Exception {
		mockMvc.perform(get(AUTH_API_LOGOUT_URL)).andExpect(status().isUnauthorized());
		Cookie cookie = registerUser(mockMvc);
		cookie = mockMvc.perform(get(PING_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(JWT_COOKIE_NAME);
		cookie = mockMvc.perform(get(AUTH_API_LOGOUT_URL).cookie(cookie)).andExpect(status().isOk())
				.andReturn().getResponse().getCookie(JWT_COOKIE_NAME);
		mockMvc.perform(get(PING_URL)).andExpect(status().isUnauthorized());
	}

	@AfterEach
	void cleanUpTable() {
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_data");
	}
}
