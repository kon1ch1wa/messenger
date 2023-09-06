package ru.shutoff.messenger.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.repository.UserInfoRepo;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ForgotCredsService {
	private final UserInfoRepo userInfoRepo;
	private final MailSender mailSender;
	private final PasswordEncoder passwordEncoder;
	private static final String MESSAGE_CHECK_LOGIN = """
			Please follow this link to remember your login on MessengerApp
			http://localhost:8080/forgotCredsApi/checkEmail?key=%s
			Do not answer on this message""";

	private static final String MESSAGE_RESTORE_PASSWORD = """
			Please follow this link to remember your login on MessengerApp
			http://localhost:8080/updateCredsApi/restorePassword?key=%s
			Do not answer on this message""";

	public String forgotLogin(String email) {
		return encodeDataAndSendEmail(email, email, MESSAGE_CHECK_LOGIN);
	}

	public String checkLogin(String key) {
		User user = userInfoRepo.getPrimary(key);
		user.setToken(null);
		userInfoRepo.update(user);
		return user.getLogin();
	}

	public String forgotPassword(String login) {
		String email = userInfoRepo.getEmailByLogin(login);
		return encodeDataAndSendEmail(login, email, MESSAGE_RESTORE_PASSWORD);
	}

	private String encodeDataAndSendEmail(String data, String receiver, String text) {
		String key = passwordEncoder.encode(data);
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(receiver);
		message.setFrom("sender.email@daemon.org");
		message.setSubject("Forgot Login on MessengerApp?");
		message.setText(String.format(text, key));
		mailSender.send(message);
		User user = userInfoRepo.getByEmail(receiver);
		user.setToken(key);
		userInfoRepo.update(user);
		return key;
	}
}
