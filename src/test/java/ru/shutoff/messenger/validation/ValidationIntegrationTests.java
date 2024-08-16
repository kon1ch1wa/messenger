package ru.shutoff.messenger.validation;

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
import ru.shutoff.messenger.dto.*;
import ru.shutoff.messenger.setup.SetupMethods;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = MessengerApplication.class)
@AutoConfigureMockMvc
public class ValidationIntegrationTests {
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

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	void runningContainerTest() {
		assertTrue(container.isRunning());
	}

	@Test
	public void registerWithInvalidDataTest() throws Exception {
		String invalidEmail = mapper.writeValueAsString(new UserPrimaryInfoDTO("not_valid_email", "valid_login", "Valid_Pass_123"));
		String nullEmail = mapper.writeValueAsString(new UserPrimaryInfoDTO(null, "valid_login", "Valid_Pass_123"));
		String invalidLogin = mapper.writeValueAsString(new UserPrimaryInfoDTO("valid_email@dev.ru", "=invalid_login=", "Valid_Pass_123"));
		String nullLogin = mapper.writeValueAsString(new UserPrimaryInfoDTO("valid_email@dev.ru", null, "Valid_Pass_123"));
		String invalidPassword = mapper.writeValueAsString(new UserPrimaryInfoDTO("valid_email@dev.ru", "valid_login", "invalid"));
		String nullPassword = mapper.writeValueAsString(new UserPrimaryInfoDTO("valid_email@dev.ru", "valid_login", null));
		mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(invalidEmail))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(nullEmail))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(invalidLogin))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(nullLogin))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(invalidPassword))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(nullPassword))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void updateWithInvalidDataTest() throws Exception {
		Cookie cookie = SetupMethods.registerUser(mockMvc);
		String invalidPhoneNumber = mapper.writeValueAsString(new UserSecondaryInfoDTO("desc", "+372587345", "url_tag"));
		String nullPhoneNumber = mapper.writeValueAsString(new UserSecondaryInfoDTO("desc", "", "url_tag"));
		String invalidUrlTag = mapper.writeValueAsString(new UserSecondaryInfoDTO("desc", "+79217642904", "_invalid_url_tag"));
		String nullUrlTag = mapper.writeValueAsString(new UserSecondaryInfoDTO("desc", "+79217642904", ""));
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(invalidPhoneNumber))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(nullPhoneNumber))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(invalidUrlTag))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(nullUrlTag))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void loginWithInvalidDataTest() throws Exception {
		String invalidLogin = mapper.writeValueAsString(new LoginRequest("-invalid_login-", "Test_Pass_0"));
		String nullLogin = mapper.writeValueAsString(new LoginRequest(null, "Test_Pass_0"));
		String invalidPassword = mapper.writeValueAsString(new LoginRequest("test_login", "notcorrcetpass"));
		String nullPassword = mapper.writeValueAsString(new LoginRequest("test_login", null));
		Cookie cookie = SetupMethods.registerUser(mockMvc);
		mockMvc.perform(get(SetupMethods.AUTH_API_LOGOUT_URL).cookie(cookie)).andExpect(status().isOk());
		mockMvc.perform(post(SetupMethods.AUTH_API_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(invalidLogin))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.AUTH_API_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(nullLogin))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.AUTH_API_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(invalidPassword))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.AUTH_API_LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(nullPassword))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void credsApiWithInvalidDataTest() throws Exception {
		String invalidOldPassword = mapper.writeValueAsString(new RestorePasswordWithAccessDto("oldpass", "New_Test_Pass_0", "New_Test_Pass_0"));
		String nullOldPassword = mapper.writeValueAsString(new RestorePasswordWithAccessDto(null, "New_Test_Pass_0", "New_Test_Pass_0"));
		String invalidNewPassword = mapper.writeValueAsString(new RestorePasswordWithAccessDto("Test_Pass_0", "newpass", "New_Test_Pass_0"));
		String nullNewPassword = mapper.writeValueAsString(new RestorePasswordWithAccessDto("Test_Pass_0", null, "New_Test_Pass_0"));
		String invalidNewPasswordConfirmation = mapper.writeValueAsString(new RestorePasswordWithAccessDto("Test_Pass_0", "New_Test_Pass_0", "newpass"));
		String nullNewPasswordConfirmation = mapper.writeValueAsString(new RestorePasswordWithAccessDto("Test_Pass_0", "New_Test_Pass_0", null));
		String invalidNewPasswordNoAccess = mapper.writeValueAsString(new RestorePasswordNoAccessDto("newpass", "New_Test_Pass_0"));
		String nullNewPasswordNoAccess = mapper.writeValueAsString(new RestorePasswordNoAccessDto(null, "New_Test_Pass_0"));
		String invalidNewPasswordConfirmationNoAccess = mapper.writeValueAsString(new RestorePasswordNoAccessDto("New_Test_Pass_0", "newpass"));
		String nullNewPasswordConfirmationNoAccess = mapper.writeValueAsString(new RestorePasswordNoAccessDto("New_Test_Pass_0", null));
		Cookie cookie = SetupMethods.registerUser(mockMvc);
		mockMvc.perform(patch(SetupMethods.UPDATE_PASSWORD_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(invalidOldPassword))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(SetupMethods.UPDATE_PASSWORD_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(nullOldPassword))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(SetupMethods.UPDATE_PASSWORD_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(invalidNewPassword))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(SetupMethods.UPDATE_PASSWORD_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(nullNewPassword))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(SetupMethods.UPDATE_PASSWORD_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(invalidNewPasswordConfirmation))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(SetupMethods.UPDATE_PASSWORD_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(nullNewPasswordConfirmation))
				.andExpect(status().isBadRequest());
		String key = mockMvc.perform(post(SetupMethods.FORGOT_PASSWORD_URL).content("test_login"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		mockMvc.perform(post(SetupMethods.UPDATE_PASSWORD_URL).param("key", key).contentType(MediaType.APPLICATION_JSON).content(invalidNewPasswordNoAccess))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.UPDATE_PASSWORD_URL).param("key", key).contentType(MediaType.APPLICATION_JSON).content(nullNewPasswordNoAccess))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.UPDATE_PASSWORD_URL).param("key", key).contentType(MediaType.APPLICATION_JSON).content(invalidNewPasswordConfirmationNoAccess))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(SetupMethods.UPDATE_PASSWORD_URL).param("key", key).contentType(MediaType.APPLICATION_JSON).content(nullNewPasswordConfirmationNoAccess))
				.andExpect(status().isBadRequest());
	}

	@AfterEach
	void cleanUpTable() {
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_data");
	}
}
