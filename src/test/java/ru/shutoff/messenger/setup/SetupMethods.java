package ru.shutoff.messenger.setup;

import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ru.shutoff.messenger.dto.UserPrimaryInfoDTO;
import ru.shutoff.messenger.dto.UserSecondaryInfoDTO;
import ru.shutoff.messenger.model.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SetupMethods {

	public static final String EMAIL = "spring.email.receiver.daemon@gmail.com";
	public static final String LOGIN = "test_login";
	public static final String PASS = "test_pass";
	public static final String DESC = "test_description";
	public static final String PHONE_NUMBER = "+79217642904";
	public static final String URL_TAG = "test_url_tag";

	public static String wrapPrimaryInfo() throws JsonProcessingException {
		UserPrimaryInfoDTO info = new UserPrimaryInfoDTO(EMAIL, LOGIN, PASS);
		return new ObjectMapper().writeValueAsString(info);
	}

	public static String wrapSecondaryInfo() throws JsonProcessingException {
		UserSecondaryInfoDTO info = new UserSecondaryInfoDTO(DESC, PHONE_NUMBER, URL_TAG);
		return new ObjectMapper().writeValueAsString(info);
	}

	public static String wrapPrimaryInfo(String email, String login, String password) throws JsonProcessingException {
		UserPrimaryInfoDTO info = new UserPrimaryInfoDTO(email, login, password);
		return new ObjectMapper().writeValueAsString(info);
	}

	public static String wrapSecondaryInfo(String description, String phoneNumber, String urlTag) throws JsonProcessingException {
		UserSecondaryInfoDTO info = new UserSecondaryInfoDTO(description, phoneNumber, urlTag);
		return new ObjectMapper().writeValueAsString(info);
	}

	public static Cookie registerUser(MockMvc mockMvc) throws Exception {
		String jsonPrimary = wrapPrimaryInfo();
		String content = mockMvc.perform(post("/authApi/user").contentType(MediaType.APPLICATION_JSON).content(jsonPrimary))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		User user = new ObjectMapper().readValue(content, User.class);
		return mockMvc.perform(get("/authApi/user").param("token", user.getToken()))
				.andExpect(status().isOk()).andReturn().getResponse().getCookie("JwtToken");
	}
}
