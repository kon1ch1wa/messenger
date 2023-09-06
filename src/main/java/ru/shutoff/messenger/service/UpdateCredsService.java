package ru.shutoff.messenger.service;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.shutoff.messenger.exception.InvalidTokenException;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.repository.UserInfoRepo;
import ru.shutoff.messenger.security.JwtUtils;

import java.util.Base64;
import java.util.Objects;

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
		User user = userInfoRepo.getPrimary(key);
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
