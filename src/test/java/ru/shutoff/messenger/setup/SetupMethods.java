package ru.shutoff.messenger.setup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import jakarta.servlet.http.Cookie;
import ru.shutoff.messenger.dto.RegisterRequest;
import ru.shutoff.messenger.dto.UpdateInfoRequest;
import ru.shutoff.messenger.model.User;

public class SetupMethods {
	public static DockerImageName postgresImageName = DockerImageName.parse("postgres:latest");
	public static DockerImageName rabbitImageName = DockerImageName.parse("rabbitmq:latest");

	public static final String EMAIL = "spring.email.receiver.daemon@gmail.com";
	public static final String LOGIN = "test_login";
	public static final String NAME = "Anton";
	public static final String PASS = "Test_Pass_0";
	public static final String DESC = "test_description";
	public static final String PHONE_NUMBER = "+79217642904";
	public static final String URL_TAG = "test_url_tag";

	public static final String AUTH_API_USER_URL = "/authApi/user";
	public static final String PING_URL = "/ping";
	public static final String AUTH_API_LOGOUT_URL = "/authApi/logout";
	public static final String AUTH_API_LOGIN_URL = "/authApi/login";

	public static final String FORGOT_LOGIN_URL = "/forgotCredsApi/forgotLogin";
	public static final String FORGOT_PASSWORD_URL = "/forgotCredsApi/forgotPassword";
	public static final String CHECK_LOGIN_URL = "/forgotCredsApi/checkLogin";
	public static final String UPDATE_PASSWORD_URL = "/updateCredsApi/restorePassword";

	public static String wrapPrimaryInfo() throws JsonProcessingException {
		RegisterRequest info = new RegisterRequest(EMAIL, LOGIN, NAME, PASS);
		return new ObjectMapper().writeValueAsString(info);
	}

	public static String wrapSecondaryInfo() throws JsonProcessingException {
		UpdateInfoRequest info = new UpdateInfoRequest(DESC, PHONE_NUMBER, URL_TAG);
		return new ObjectMapper().writeValueAsString(info);
	}

	public static String wrapPrimaryInfo(String email, String login, String name, String password) throws JsonProcessingException {
		RegisterRequest info = new RegisterRequest(email, login, name, password);
		return new ObjectMapper().writeValueAsString(info);
	}

	public static String wrapSecondaryInfo(String description, String phoneNumber, String urlTag) throws JsonProcessingException {
		UpdateInfoRequest info = new UpdateInfoRequest(description, phoneNumber, urlTag);
		return new ObjectMapper().writeValueAsString(info);
	}

	public static User registerUser(MockMvc mockMvc) throws Exception {
		String jsonPrimary = wrapPrimaryInfo();
		String content = mockMvc.perform(post(AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(jsonPrimary))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return new ObjectMapper().readValue(content, User.class);
	}

	public static User registerAnotherUser(MockMvc mockMvc) throws Exception {
		String jsonPrimary = wrapPrimaryInfo("spring@dev.ru", "test_another_login", "Vladimir", "Test_Another_Pass_0");
		String content = mockMvc.perform(post(AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(jsonPrimary))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return new ObjectMapper().readValue(content, User.class);
	}

	public static User registerThirdUser(MockMvc mockMvc) throws Exception {
		String jsonPrimary = wrapPrimaryInfo("spring@gov.ru", "test_third_login", "Vitaliy", "Test_Third_Pass_0");
		String content = mockMvc.perform(post(AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(jsonPrimary))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return new ObjectMapper().readValue(content, User.class);
	}

	public static Cookie activateUser(MockMvc mockMvc, User user) throws Exception {
		return mockMvc.perform(get(AUTH_API_USER_URL).param("token", user.getToken()))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie("JwtToken");
	}

	public static Pair<User, Cookie> registerWithPairReturned(MockMvc mockMvc, String email, String login, String name, String password) throws Exception {
		String jsonPrimary = wrapPrimaryInfo(email, login, name, password);
		String content = mockMvc.perform(post(AUTH_API_USER_URL).contentType(MediaType.APPLICATION_JSON).content(jsonPrimary))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = new ObjectMapper().readValue(content, User.class);
		var response = mockMvc.perform(get(AUTH_API_USER_URL).param("token", user.getToken()))
				.andExpect(status().isOk()).andReturn().getResponse();
		return Pair.of(new ObjectMapper().readValue(response.getContentAsString(), User.class), response.getCookie("JwtToken"));
	}
}
