package ru.shutoff.messenger.domain.user_mgmt.service;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.user_mgmt.model.User;
import ru.shutoff.messenger.domain.user_mgmt.repository.UserInfoRepo;

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
		User user = userInfoRepo.getByUnqiueToken(key);
		user.setToken(null);
		userInfoRepo.update(user);
		return user.getLogin();
	}

	public String forgotPassword(String login) {
		String email = userInfoRepo.getByLogin(login).getEmail();
		return encodeDataAndSendEmail(login, email, MESSAGE_RESTORE_PASSWORD);
	}

	private String encodeDataAndSendEmail(String data, String receiverEmail, String text) {
		String key = passwordEncoder.encode(data);
		key = key.substring(7);
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(receiverEmail);
		message.setFrom("sender.email@daemon.org");
		message.setSubject("Forgot credentials on MessengerApp?");
		message.setText(String.format(text, key));
		mailSender.send(message);
		User user = userInfoRepo.getByEmail(receiverEmail);
		user.setToken(key);
		userInfoRepo.update(user);
		return key;
	}
}
