package ru.shutoff.messenger.service;

import java.util.UUID;

import org.springframework.data.util.Pair;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.exception.NotAuthorizedException;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.repository.UserInfoRepo;
import ru.shutoff.messenger.security.JwtUtils;

@Service
@RequiredArgsConstructor
public class AuthApiService {
	private final UserInfoRepo userInfoRepo;
	private final MailSender mailSender;
	private final JwtUtils jwtUtils;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;

	private final UserDetailsService userDetailsService;

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
		userInfoRepo.primarySave(user);
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

	public Pair<User, String> endRegistration(String token) {
		User user = userInfoRepo.getPrimary(token);
		user.setToken(null);
		user.setActivated(true);
		userInfoRepo.update(user);
		userInfoRepo.deleteToken(token);
		String userPassword = user.getPassword();
		user.setPassword(passwordEncoder.encode(userPassword));
		userInfoRepo.update(user);
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				user.getLogin(),
				userPassword
		));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwtToken = jwtUtils.generateJwtToken(authentication, user.getLogin(), user.getPassword());
		return Pair.of(user, jwtToken);
	}

	public User updateUser(String jwtToken, String description, String phoneNumber, String urlTag) {
		String login = jwtUtils.getUsernameFromJwtToken(jwtToken);
		User user = userInfoRepo.getByLogin(login);
		if (description != null) {
			user.setDescription(description);
			userInfoRepo.updateValueByLogin("description", description, login);
		}
		if (phoneNumber != null) {
			user.setPhoneNumber(phoneNumber);
			userInfoRepo.updateValueByLogin("phone_number", phoneNumber, login);
		}
		if (urlTag != null) {
			user.setUrlTag(urlTag);
			userInfoRepo.updateValueByLogin("url_tag", urlTag, login);
		}
		return user;
	}

	public String login(String login, String password) {
		try {
			User user = userInfoRepo.getByLogin(login);
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							login,
							password
					)
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			return jwtUtils.generateJwtToken(authentication, user.getLogin(), user.getPassword());
		} catch (UsernameNotFoundException | BadCredentialsException ex) {
			throw new NotAuthorizedException("Not authorized: Bad credentials");
		}
	}

	public void logout(Cookie cookie) {
		jwtUtils.invalidateJwtToken(cookie);
	}

	/*
	TODO Написать валидацию для данных, которые поступают на вход
	TODO Исключения, если значение поля, которое должно быть уникальным уже существует или валидация не прошла
	TODO Перепривязка почтового ящика только при доступе к текущему ящику
	TODO Обновление пароля только при доступе к текущему паролю
	TODO Забыли логин
	TODO Забыли пароль
	TODO Покрыть тестами
	TODO ПРИКРУТИТЬ REDIS, RABBITMQ
	TODO ЧАТ + РАЗОБРАТЬСЯ С WEBSOCKET
	*/
}
