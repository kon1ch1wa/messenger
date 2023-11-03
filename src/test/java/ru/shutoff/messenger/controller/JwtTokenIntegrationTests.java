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
import ru.shutoff.messenger.setup.SetupMethods;
import ru.shutoff.messenger.setup.TestConfiguration;

import java.util.concurrent.TimeUnit;

import static ru.shutoff.messenger.setup.SetupMethods.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = MessengerApplication.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc
public class JwtTokenIntegrationTests {
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
		registry.add("jwt.expiration_time", () -> "2000");
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private String jwtCookieName;

	@Test
	void runningContainerTest() {
		assertTrue(container.isRunning());
	}

	@Test
	void pingUnauthorizedNotCreatingCookieTest() throws Exception {
		Cookie cookie = mockMvc.perform(get(PING_URL))
				.andExpect(status().isUnauthorized()).andReturn().getResponse().getCookie(jwtCookieName);
		assertNull(cookie);
	}

	@Test
	void pingAuthorizedCookieTest() throws Exception {
		Cookie cookie = activateUser(mockMvc, registerUser(mockMvc));
		mockMvc.perform(get(PING_URL).cookie(cookie)).andExpect(status().isOk());
	}

	@Test
	void pingUpdatingCookieTest() throws Exception {
		mockMvc.perform(get(PING_URL)).andExpect(status().isUnauthorized());
		Cookie cookie = activateUser(mockMvc, registerUser(mockMvc));
		Cookie cookieNew = mockMvc.perform(get(PING_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		assertNotNull(cookieNew);
		mockMvc.perform(get(PING_URL).cookie(cookieNew)).andExpect(status().isOk());
	}

	@Test
	void pingUpdatingCookieLifecycleTest() throws Exception {
		Cookie cookie = activateUser(mockMvc, registerUser(mockMvc));

		cookie = mockMvc.perform(get(PING_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		Thread.sleep(TimeUnit.SECONDS.toMillis(1));
		cookie = mockMvc.perform(get(PING_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		Thread.sleep(TimeUnit.SECONDS.toMillis(2));
		mockMvc.perform(get(PING_URL)).andExpect(status().isUnauthorized());
	}

	@Test
	void loginTest() throws Exception {
		mockMvc.perform(get(AUTH_API_LOGOUT_URL)).andExpect(status().isUnauthorized());
		Cookie cookie = activateUser(mockMvc, registerUser(mockMvc));

		cookie = mockMvc.perform(get(PING_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		cookie = mockMvc.perform(get(AUTH_API_LOGOUT_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		mockMvc.perform(get(PING_URL)).andExpect(status().isUnauthorized());

		String authJson = mapper.writeValueAsString(new LoginRequest(LOGIN, PASS));
		Cookie authCookie = mockMvc.perform(post(AUTH_API_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(authJson))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		authCookie = mockMvc.perform(get(PING_URL).cookie(authCookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		mockMvc.perform(get(AUTH_API_LOGOUT_URL).cookie(authCookie)).andExpect(status().isOk());

		Cookie authCookieNew = mockMvc.perform(post(AUTH_API_LOGIN_URL))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getCookie(jwtCookieName);
		assertNull(authCookieNew);
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
		Cookie cookie = activateUser(mockMvc, registerUser(mockMvc));
		cookie = mockMvc.perform(get(PING_URL).cookie(cookie))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie(jwtCookieName);
		cookie = mockMvc.perform(get(AUTH_API_LOGOUT_URL).cookie(cookie)).andExpect(status().isOk())
				.andReturn().getResponse().getCookie(jwtCookieName);
		mockMvc.perform(get(PING_URL)).andExpect(status().isUnauthorized());
	}

	@AfterEach
	void cleanUpTable() {
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_data");
	}
}
