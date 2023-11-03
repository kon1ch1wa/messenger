package ru.shutoff.messenger.chat_logic;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ru.shutoff.messenger.chat_logic.dto.MessageDto;
import ru.shutoff.messenger.chat_logic.model.ChatRoom;
import ru.shutoff.messenger.chat_logic.model.Message;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.setup.SetupMethods;
import ru.shutoff.messenger.setup.TestConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc
public class ChatTests {
	@Container
	private static final PostgreSQLContainer<?> container = SetupMethods.container;
	@Container
	private static final RabbitMQContainer rabbitMqContainer = new RabbitMQContainer("rabbitmq:3.12.4-management")
			.withPluginsEnabled("rabbitmq_web_stomp")
			.withUser("RMQAdmin", "RMQPassword")
			.withExposedPorts(5672, 15672);

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", container::getJdbcUrl);
		registry.add("spring.datasource.username", container::getUsername);
		registry.add("spring.datasource.password", container::getPassword);
		registry.add("spring.liquibase.url", container::getJdbcUrl);
		registry.add("spring.liquibase.user", container::getUsername);
		registry.add("spring.liquibase.password", container::getPassword);
	}

	@Value("${local.server.port}")
	private int port;
	private StompSession stompSession1;
	private StompSession stompSession2;
	private StompSession stompSession3;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper mapper;

	private String wrapMessage(String content, String receiverId, String chatRoomId) throws JsonProcessingException {
		return mapper.writeValueAsString(new MessageDto(content, receiverId, chatRoomId));
	}

	@Test
	void runningContainerTest() {
		assertTrue(container.isRunning());
		assertTrue(rabbitMqContainer.isRunning());
	}

	@BeforeEach
	public void setUp() throws Exception {
		String url = "ws://localhost:" + port + "/ws-endpoint";
		WebSocketStompClient stompClient1 = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
		stompClient1.setMessageConverter(new MappingJackson2MessageConverter());
		WebSocketStompClient stompClient2 = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
		stompClient2.setMessageConverter(new MappingJackson2MessageConverter());
		WebSocketStompClient stompClient3 = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
		stompClient3.setMessageConverter(new MappingJackson2MessageConverter());
		stompSession1 = stompClient1.connectAsync(url, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
		stompSession2 = stompClient2.connectAsync(url, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
		stompSession3 = stompClient3.connectAsync(url, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
	}

	@Test
	public void chatLogicTest() throws Exception {
		User user1 = SetupMethods.registerUser(mockMvc);
		Cookie cookie1 = SetupMethods.activateUser(mockMvc, user1);
		User user2 = SetupMethods.registerAnotherUser(mockMvc);
		Cookie cookie2 = SetupMethods.activateUser(mockMvc, user2);
		User user3 = SetupMethods.registerThirdUser(mockMvc);
		Cookie cookie3 = SetupMethods.activateUser(mockMvc, user3);
		String chatRoom12Str = mockMvc.perform(
			get("/chat/{receiverUrlTag}/room", user2.getId())
				.cookie(cookie1)
		)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		ChatRoom chatRoom12 = mapper.readValue(chatRoom12Str, ChatRoom.class);

		String chatRoom32Str = mockMvc.perform(
			get("/chat/{receiverUrlTag}/room", user2.getId())
				.cookie(cookie3)
		)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		ChatRoom chatRoom32 = mapper.readValue(chatRoom32Str, ChatRoom.class);

		String chatRoom13Str = mockMvc.perform(
			get("/chat/{receiverUrlTag}/room", user3.getId())
				.cookie(cookie1)
		)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		ChatRoom chatRoom13 = mapper.readValue(chatRoom13Str, ChatRoom.class);
		StompFrameHandler stompFrameHandler = new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return String.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				return;
			}
		};
		stompSession1.subscribe("/queue/messages/" + chatRoom12.getChatRoomId(), stompFrameHandler);
		stompSession1.subscribe("/queue/messages/" + chatRoom13.getChatRoomId(), stompFrameHandler);
		stompSession2.subscribe("/queue/messages/" + chatRoom12.getChatRoomId(), stompFrameHandler);
		stompSession2.subscribe("/queue/messages/" + chatRoom32.getChatRoomId(), stompFrameHandler);
		stompSession3.subscribe("/queue/messages/" + chatRoom13.getChatRoomId(), stompFrameHandler);
		stompSession3.subscribe("/queue/messages/" + chatRoom32.getChatRoomId(), stompFrameHandler);

		/*stompSession2.send("/chat/" + chatRoom12.getChatRoomId() + "/send-message", "Hi!");
		stompSession2.send("/chat/" + chatRoom32.getChatRoomId() + "/send-message", "User#2 ping to user#3");
		stompSession1.send("/chat/" + chatRoom12.getChatRoomId() + "/send-message", "Hello, who are you?");
		stompSession2.send("/chat/" + chatRoom12.getChatRoomId() + "/send-message", "I am user #2");
		stompSession1.send("/chat/" + chatRoom12.getChatRoomId() + "/send-message", "Hello user#2!");
		stompSession3.send("/chat/" + chatRoom13.getChatRoomId() + "/send-message", "Hi user#1!");
		stompSession1.send("/chat/" + chatRoom13.getChatRoomId() + "/send-message", "Hi user#3!");
		stompSession3.send("/chat/" + chatRoom13.getChatRoomId() + "/send-message", "Let's go outside?");
		stompSession1.send("/chat/" + chatRoom13.getChatRoomId() + "/send-message", "Nah, sorry, I am too busy");
		stompSession3.send("/chat/" + chatRoom32.getChatRoomId() + "/send-message", "Hello user#2! I'm glad you pinged me.");
		stompSession3.send("/chat/" + chatRoom13.getChatRoomId() + "/send-message", "Ok. :(");*/

		String message;
		message = wrapMessage("Hi!", user1.getId(), chatRoom12.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie2)).andExpect(status().isOk());
		
		message = wrapMessage("User#2 ping to user#3", user3.getId(), chatRoom32.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom32.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie2)).andExpect(status().isOk());
		
		message = wrapMessage("Hello, who are you?", user2.getId(), chatRoom12.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isOk());
		
		message = wrapMessage("I am user#2", user1.getId(), chatRoom12.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie2)).andExpect(status().isOk());
		
		message = wrapMessage("Hi, user#2!", user2.getId(), chatRoom12.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isOk());

		message = wrapMessage("Sup, user#1.", user1.getId(), chatRoom13.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isOk());

		message = wrapMessage("Yo, I'm nice!", user3.getId(), chatRoom13.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isOk());

		message = wrapMessage("Let's go for a ride!", user1.getId(), chatRoom13.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isOk());

		message = wrapMessage("Nah, sry, I am too busy rn!", user3.getId(), chatRoom13.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isOk());

		message = wrapMessage("Hello, user#2. I am glad you pinged me!", user2.getId(), chatRoom32.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom32.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isOk());

		message = wrapMessage("Ok. :-(", user1.getId(), chatRoom13.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isOk());

		message = wrapMessage("User#3 interception", user1.getId(), chatRoom12.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isUnauthorized());
		message = wrapMessage("User#3 interception", user2.getId(), chatRoom12.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isUnauthorized());

		message = wrapMessage("User#2 interception", user1.getId(), chatRoom13.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie2)).andExpect(status().isUnauthorized());
		message = wrapMessage("User#2 interception", user3.getId(), chatRoom13.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie2)).andExpect(status().isUnauthorized());

		message = wrapMessage("User#1 interception", user3.getId(), chatRoom32.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom32.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isUnauthorized());
		message = wrapMessage("User#1 interception", user2.getId(), chatRoom32.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom32.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isUnauthorized());

		String response1;
		String response2;
		String response3;
		List<Message> messages1;
		List<Message> messages2;
		List<Message> messages3;
		TypeReference<List<Message>> reference = new TypeReference<List<Message>>(){};
		response1 = mockMvc.perform(get("/chat/{chatRoomId}/messages", chatRoom12.getChatRoomId()).cookie(cookie1)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		response2 = mockMvc.perform(get("/chat/{chatRoomId}/messages", chatRoom12.getChatRoomId()).cookie(cookie2)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		messages1 = mapper.readValue(response1, reference);
		messages2 = mapper.readValue(response2, reference);
		assertEquals(messages1.size(), messages2.size());
		assertEquals(messages1.size(), 4);
		response1 = mockMvc.perform(get("/chat/{chatRoomId}/messages", chatRoom13.getChatRoomId()).cookie(cookie1)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		response3 = mockMvc.perform(get("/chat/{chatRoomId}/messages", chatRoom13.getChatRoomId()).cookie(cookie3)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		messages1 = mapper.readValue(response1, reference);
		messages3 = mapper.readValue(response3, reference);
		assertEquals(messages1.size(), messages3.size());
		assertEquals(messages1.size(), 5);
		response2 = mockMvc.perform(get("/chat/{chatRoomId}/messages", chatRoom32.getChatRoomId()).cookie(cookie2)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		response3 = mockMvc.perform(get("/chat/{chatRoomId}/messages", chatRoom32.getChatRoomId()).cookie(cookie3)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		messages2 = mapper.readValue(response2, reference);
		messages3 = mapper.readValue(response3, reference);
		assertEquals(messages2.size(), messages3.size());
		assertEquals(messages2.size(), 2);


		response2 = mockMvc.perform(get("/chat/{chatRoomId}/messages", chatRoom12.getChatRoomId()).cookie(cookie3)).andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
		response2 = mockMvc.perform(get("/chat/{chatRoomId}/messages", chatRoom13.getChatRoomId()).cookie(cookie2)).andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
		response2 = mockMvc.perform(get("/chat/{chatRoomId}/messages", chatRoom32.getChatRoomId()).cookie(cookie1)).andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
	}

	@Test
	public void userInfoTest() throws Exception {
		User user1 = SetupMethods.registerUser(mockMvc);
		Cookie cookie1 = SetupMethods.activateUser(mockMvc, user1);
		String json1 = SetupMethods.wrapSecondaryInfo(null, null, "UrlTag1");
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie1).contentType(MediaType.APPLICATION_JSON).content(json1));
		User user2 = SetupMethods.registerAnotherUser(mockMvc);
		Cookie cookie2 = SetupMethods.activateUser(mockMvc, user2);
		String json2 = SetupMethods.wrapSecondaryInfo(null, null, "UrlTag2");
		mockMvc.perform(patch(SetupMethods.AUTH_API_USER_URL).cookie(cookie2).contentType(MediaType.APPLICATION_JSON).content(json2)).andExpect(status().isOk());

		String user2Id = mockMvc.perform(get("/chat/{receiverUrlTag}/info", user2.getId()).cookie(cookie1)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String user1Id = mockMvc.perform(get("/chat/{receiverUrlTag}/info", user1.getId()).cookie(cookie2)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String user2Tag = mockMvc.perform(get("/chat/{receiverUrlTag}/info", "UrlTag2").cookie(cookie1)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String user1Tag = mockMvc.perform(get("/chat/{receiverUrlTag}/info", "UrlTag1").cookie(cookie2)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		mockMvc.perform(get("/chat/{receiverUrlTag}/info", UUID.randomUUID().toString()).cookie(cookie1)).andExpect(status().isNotFound());
		mockMvc.perform(get("/chat/{receiverUrlTag}/info", UUID.randomUUID().toString()).cookie(cookie2)).andExpect(status().isNotFound());
		assertEquals(user1Tag, user1Id);
		assertEquals(user2Tag, user2Id);
	}

	@Test
	public void getChatRoomTest() throws Exception {
		User user1 = SetupMethods.registerUser(mockMvc);
		Cookie cookie1 = SetupMethods.activateUser(mockMvc, user1);
		User user2 = SetupMethods.registerAnotherUser(mockMvc);
		Cookie cookie2 = SetupMethods.activateUser(mockMvc, user2);
		mockMvc.perform(get("/chat/{receiverUrlTag}/room", user2.getId()).cookie(cookie1));
		mockMvc.perform(get("/chat/{receiverUrlTag}/room", user2.getId()).cookie(cookie2));
	}

	@AfterEach
	public void cleanUp() {
		stompSession1.disconnect();
		stompSession2.disconnect();
		stompSession3.disconnect();
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "messages");
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "chat_rooms");
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_data");
	}
}
