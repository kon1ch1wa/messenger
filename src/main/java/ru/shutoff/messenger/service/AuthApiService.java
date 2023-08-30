package ru.shutoff.messenger.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.repository.UserInfoRepo;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthApiService {
	private final UserInfoRepo userInfoRepo;

	public void register(String email, String login, String password) {
		User user = new User().builder()
				.id(UUID.randomUUID())
				.email(email)
				.login(login)
				.password(password)
				.build();
		userInfoRepo.save(user);
	}
}
