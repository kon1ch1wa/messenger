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

	public void register(String email, String login, String password) {
		User user = new User().builder()
				.id(UUID.randomUUID())
				.email(email)
				.login(login)
				.password(password)
				.build();
		userInfoRepo.save(user);
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setText("Please, confirm that you are not a robot by entering this link.");
			message.setTo(email);
			mailSender.send(message);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
