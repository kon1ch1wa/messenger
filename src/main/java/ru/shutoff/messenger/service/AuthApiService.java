package ru.shutoff.messenger.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.repository.UserInfoRepo;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthApiService {
	private final UserInfoRepo userInfoRepo;
	private final MailSender mailSender;

	private static final String MESSAGE_MAIL = """
			Please follow this link to verify your email and end registration on MessengerApp
			http://localhost:8080/user/endRegistration?token=%s
			Do not answer on this message""";

	public User register(String email, String login, String password) {
		String token = UUID.randomUUID().toString();
		User user = new User().builder()
				.id(UUID.randomUUID())
				.email(email)
				.login(login)
				.password(password)
				.isActivated(false)
				.token(token)
				.build();
		userInfoRepo.save(user);
		userInfoRepo.addToken(user.getId(), token);
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setSubject("Confirmation Email on MessengerApp");
			message.setText(String.format(MESSAGE_MAIL, token));
			message.setFrom("sender.email@daemon.org");
			message.setTo(email);
			mailSender.send(message);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return user;
	}

	public User endRegistration(String token) {
		User user = userInfoRepo.getByToken(token);
		user.setToken(null);
		user.setActivated(true);
		userInfoRepo.deleteToken(token);
		return user;
	}
}
