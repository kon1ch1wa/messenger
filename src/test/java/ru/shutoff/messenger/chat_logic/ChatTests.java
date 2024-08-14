package ru.shutoff.messenger.chat_logic;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.shutoff.messenger.setup.SetupMethods.rabbitImageName;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import ru.shutoff.messenger.chat_logic.dto.MessageDto;
import ru.shutoff.messenger.setup.SetupMethods;
import ru.shutoff.messenger.setup.TestConfiguration;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc
public class ChatTests {
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

	@Value("${local.server.port}")
	private int port;

	private StompSession stompSession1;
	private StompSession stompSession2;
	private StompSession stompSession3;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ObjectMapper mapper;

	private String wrapMessage(UUID chatRoomId, UUID senderId, String content) throws JsonProcessingException {
		String _chatRoomId = chatRoomId.toString();
		String _senderId = senderId.toString();
		return mapper.writeValueAsString(new MessageDto(_chatRoomId, _senderId, content, new Timestamp(0)));
	}

	private String wrapMessage(String chatRoomId, String senderId, String content) throws JsonProcessingException {
		return mapper.writeValueAsString(new MessageDto(chatRoomId, senderId, content, new Timestamp(0)));
	}

	@Test
	void runningContainerTest() {
		assertTrue(postgresContainer.isRunning());
		assertTrue(rabbitMqContainer.isRunning());
	}

	@BeforeEach
	public void setUp() throws Exception {
		String url = "ws://localhost:" + port + "/websocket";
		WebSocketStompClient stompClient1 = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
		stompClient1.setMessageConverter(new MappingJackson2MessageConverter());
		CompletableFuture<StompSession> fSS1 = stompClient1.connectAsync(url, new StompSessionHandlerAdapter() {});
		stompSession1 = fSS1.get();
		WebSocketStompClient stompClient2 = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
		stompClient2.setMessageConverter(new MappingJackson2MessageConverter());
		CompletableFuture<StompSession> fSS2 = stompClient2.connectAsync(url, new StompSessionHandlerAdapter() {});
		stompSession2 = fSS2.get();
		WebSocketStompClient stompClient3 = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
		stompClient3.setMessageConverter(new MappingJackson2MessageConverter());
		CompletableFuture<StompSession> fSS3 = stompClient3.connectAsync(url, new StompSessionHandlerAdapter() {});
		stompSession3 = fSS3.get();
	}

	@Test
	public void chatLogicTest() throws Exception {
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
		String chatRoomId12 = UUID.randomUUID().toString();
		String chatRoomId13 = UUID.randomUUID().toString();
		String chatRoomId23 = UUID.randomUUID().toString();
		String userId1 = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String userId3 = UUID.randomUUID().toString();
		stompSession1.subscribe("/exchange/rabbitmq.chat.exchange/" + chatRoomId12, stompFrameHandler);
		stompSession1.subscribe("/exchange/rabbitmq.chat.exchange/" + chatRoomId13, stompFrameHandler);
		stompSession2.subscribe("/exchange/rabbitmq.chat.exchange/" + chatRoomId12, stompFrameHandler);
		stompSession2.subscribe("/exchange/rabbitmq.chat.exchange/" + chatRoomId23, stompFrameHandler);
		stompSession3.subscribe("/exchange/rabbitmq.chat.exchange/" + chatRoomId13, stompFrameHandler);
		stompSession3.subscribe("/exchange/rabbitmq.chat.exchange/" + chatRoomId23, stompFrameHandler);

		String destination = "/app/send";
		stompSession2.send(destination, wrapMessage(chatRoomId12, userId2, "Hi!"));
		stompSession2.send(destination, wrapMessage(chatRoomId23, userId3, "User#2 ping to user#3"));
		stompSession1.send(destination, wrapMessage(chatRoomId12, userId1, "Hello, who are you?"));
		stompSession2.send(destination, wrapMessage(chatRoomId12, userId2, "I am user #2"));
		stompSession1.send(destination, wrapMessage(chatRoomId12, userId1, "Hello, user#2!"));
		stompSession3.send(destination, wrapMessage(chatRoomId13, userId3, "Hi, user#1!"));
		stompSession1.send(destination, wrapMessage(chatRoomId13, userId1, "Hi, user#3!"));
		stompSession3.send(destination, wrapMessage(chatRoomId13, userId3, "Let's go outside?"));
		stompSession1.send(destination, wrapMessage(chatRoomId13, userId1, "Nah, sorry, I am too busy"));
		stompSession3.send(destination, wrapMessage(chatRoomId23, userId3, "Hello user#2! I'm glad you pinged me."));
		stompSession3.send(destination, wrapMessage(chatRoomId13, userId3, "Ok. :("));

		/*String message;
		message = wrapMessage(chatRoom12.getChatRoomId(), user2.getId(), "Hi!");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie2)).andExpect(status().isOk());
		
		message = wrapMessage(chatRoom32.getChatRoomId(), user2.getId(), "User#2 ping to user#3");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom32.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie2)).andExpect(status().isOk());
		
		message = wrapMessage(chatRoom12.getChatRoomId(), user1.getId(), "Hello, who are you?");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isOk());
		
		message = wrapMessage(chatRoom12.getChatRoomId(), user2.getId(), "I am user#2");
		mockMvc.perform(post("/chat/{chatRoomId}/send-messge", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie2)).andExpect(status().isOk());
		
		message = wrapMessage(chatRoom12.getChatRoomId(), user1.getId(), "Hi, user#2!");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isOk());

		message = wrapMessage(chatRoom13.getChatRoomId(), user3.getId(), "Sup, user#1.");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isOk());

		message = wrapMessage(chatRoom13.getChatRoomId(), user1.getId(), "Yo, I'm nice!");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isOk());

		message = wrapMessage("Let's go for a ride!", user3.getId(), chatRoom13.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isOk());

		message = wrapMessage("Nah, sry, I am too busy rn!", user1.getId(), chatRoom13.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isOk());

		message = wrapMessage("Hello, user#2. I am glad you pinged me!", user3.getId(), chatRoom32.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom32.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isOk());

		message = wrapMessage("Ok. :-(", user3.getId(), chatRoom13.getChatRoomId());
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isOk());

		message = wrapMessage(chatRoom12.getChatRoomId(), user2.getId(), "User#3 interception");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isUnauthorized());
		message = wrapMessage(chatRoom12.getChatRoomId(), user1.getId(), "User#3 interception");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom12.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie3)).andExpect(status().isUnauthorized());

		message = wrapMessage(chatRoom13.getChatRoomId(), user3.getId(), "User#2 interception");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie2)).andExpect(status().isUnauthorized());
		message = wrapMessage(chatRoom13.getChatRoomId(), user1.getId(), "User#2 interception");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom13.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie2)).andExpect(status().isUnauthorized());

		message = wrapMessage(chatRoom32.getChatRoomId(), user2.getId(), "User#1 interception");
		mockMvc.perform(post("/chat/{chatRoomId}/send-message", chatRoom32.getChatRoomId()).contentType(MediaType.APPLICATION_JSON).content(message).cookie(cookie1)).andExpect(status().isUnauthorized());
		message = wrapMessage(chatRoom32.getChatRoomId(), user3.getId(), "User#1 interception");
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
		response2 = mockMvc.perform(get("/chat/{chatRoomId}/messages", chatRoom32.getChatRoomId()).cookie(cookie1)).andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();*/
	}

	@AfterEach
	public void cleanUp() {
		if (stompSession1 != null) {
			stompSession1.disconnect();
		}
		if (stompSession2 != null) {
			stompSession2.disconnect();
		}
		if (stompSession3 != null) {
			stompSession3.disconnect();
		}
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "messages");
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "chat_rooms");
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "user_and_chat_room");
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_data");
	}
}
