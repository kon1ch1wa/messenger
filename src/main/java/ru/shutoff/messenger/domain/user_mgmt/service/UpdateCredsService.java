package ru.shutoff.messenger.domain.user_mgmt.service;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.user_mgmt.exception.InvalidTokenException;
import ru.shutoff.messenger.domain.user_mgmt.model.User;
import ru.shutoff.messenger.domain.user_mgmt.repository.UserInfoRepo;
import ru.shutoff.messenger.domain.user_mgmt.security.JwtUtils;

@Service
@RequiredArgsConstructor
public class UpdateCredsService {
	private final UserInfoRepo userInfoRepo;
	private final JwtUtils jwtUtils;
	private final PasswordEncoder passwordEncoder;

	public User restorePasswordThroughKey(String key, String password, String confirmPassword) {
		if (!Objects.equals(password, confirmPassword)) {
			throw new InvalidTokenException("Passwords are not equal");
		}
		User user = userInfoRepo.getByUnqiueToken(key);
		user.setToken(null);
		user.setPassword(passwordEncoder.encode(password));
		userInfoRepo.update(user);
		return user;
	}

	public User restorePasswordThroughJwt(Cookie jwtCookie, String password, String confirmPassword, String oldPassword) {
		if (!Objects.equals(password, confirmPassword)) {
			throw new InvalidTokenException("New passwords are not equal");
		}
		String login = jwtUtils.getUsernameFromJwtToken(jwtCookie.getValue());
		User user = userInfoRepo.getByLogin(login);
		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			throw new InvalidTokenException("Old and new passwords are not equal");
		}
		user.setPassword(passwordEncoder.encode(password));
		userInfoRepo.update(user);
		return user;
	}
}
