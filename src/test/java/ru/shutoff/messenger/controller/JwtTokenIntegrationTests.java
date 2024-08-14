package ru.shutoff.messenger.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.shutoff.messenger.setup.SetupMethods.AUTH_API_LOGIN_URL;
import static ru.shutoff.messenger.setup.SetupMethods.AUTH_API_LOGOUT_URL;
import static ru.shutoff.messenger.setup.SetupMethods.LOGIN;
import static ru.shutoff.messenger.setup.SetupMethods.PASS;
import static ru.shutoff.messenger.setup.SetupMethods.PING_URL;
import static ru.shutoff.messenger.setup.SetupMethods.activateUser;
import static ru.shutoff.messenger.setup.SetupMethods.rabbitImageName;
import static ru.shutoff.messenger.setup.SetupMethods.registerUser;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import ru.shutoff.messenger.MessengerApplication;
import ru.shutoff.messenger.dto.LoginRequest;
import ru.shutoff.messenger.setup.SetupMethods;
import ru.shutoff.messenger.setup.TestConfiguration;

@Testcontainers
@SpringBootTest(classes = MessengerApplication.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc
public class JwtTokenIntegrationTests {
	public static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(SetupMethods.postgresImageName)
			.withUsername("admin")
			.withPassword("admin")
			.withDatabaseName("messenger_db");

	public static final RabbitMQContainer rabbitMqContainer = new RabbitMQContainer(rabbitImageName)
			.withPluginsEnabled("rabbitmq_stomp", "rabbitmq_web_stomp")
			.withEnv("RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS", "-rabbit disk_free_limit 2147483648")
			.withEnv("NODENAME", "rabbitmq@rabbitmq")
			.withEnv("HOSTNAME", "rabbitmq")
			.withExposedPorts(5672, 15672, 61613);

	@BeforeAll
	static void beforeAll() {
		postgresContainer.start();
		rabbitMqContainer.start();
	}

	@AfterAll
	static void afterAll() {
		postgresContainer.stop();
		postgresContainer.close();
		rabbitMqContainer.stop();
		rabbitMqContainer.close();
	}

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresContainer::getUsername);
		registry.add("spring.datasource.password", postgresContainer::getPassword);
		registry.add("spring.liquibase.url", postgresContainer::getJdbcUrl);
		registry.add("spring.liquibase.user", postgresContainer::getUsername);
		registry.add("spring.liquibase.password", postgresContainer::getPassword);
		registry.add("spring.rabbitmq.username", rabbitMqContainer::getAdminUsername);
		registry.add("spring.rabbitmq.password", rabbitMqContainer::getAdminPassword);
		registry.add("spring.rabbitmq.port", () -> rabbitMqContainer.getMappedPort(5672));
		registry.add("spring.rabbitmq.stomp-port", () -> rabbitMqContainer.getMappedPort(61613));
		registry.add("spring.rabbitmq.host", () -> "localhost");
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
		assertTrue(postgresContainer.isRunning());
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
