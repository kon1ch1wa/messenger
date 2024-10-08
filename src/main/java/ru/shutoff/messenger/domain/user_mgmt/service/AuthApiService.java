package ru.shutoff.messenger.domain.user_mgmt.service;

import java.util.UUID;

import org.springframework.data.util.Pair;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shutoff.messenger.domain.user_mgmt.exception.NotAuthorizedException;
import ru.shutoff.messenger.domain.user_mgmt.model.User;
import ru.shutoff.messenger.domain.user_mgmt.repository.UserInfoRepo;
import ru.shutoff.messenger.domain.user_mgmt.security.JwtUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApiService {
	private final UserInfoRepo userInfoRepo;
	private final MailSender mailSender;
	private final JwtUtils jwtUtils;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;

	private static final String MESSAGE_MAIL = """
			Please follow this link to verify your email and end registration on MessengerApp
			http://localhost:8080/authApi/user?token=%s
			Do not answer on this message""";

	public User register(String email, String login, String name, String password) {
		String token = UUID.randomUUID().toString().replace("-", "");
		User user = User.builder()
				.id(UUID.randomUUID())
				.email(email)
				.login(login)
				.name(name)
				.password(password)
				.isActivated(false)
				.token(token)
				.build();
		log.debug("Save user in repository: {}", user.toString());
		userInfoRepo.save(user);
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(email);
			message.setFrom("sender.email@daemon.org");
			message.setSubject("Confirmation Email on MessengerApp");
			message.setText(String.format(MESSAGE_MAIL, token));
			mailSender.send(message);
			log.debug("Sending Message to user: {}", user.toString());
		} catch (MailException ex) {
			log.error("Could not send message: {}", ex.getMessage());
		}
		return user;
	}

	public Pair<User, String> endRegistration(String token) {
		User user = userInfoRepo.getByUnqiueToken(token);
		user.setToken(null);
		user.setActivated(true);
		String userPassword = user.getPassword();
		user.setPassword(passwordEncoder.encode(userPassword));
		userInfoRepo.update(user);
		log.debug("User activated: {}", user.toString());
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
				user.getLogin(),
				userPassword
			)
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwtToken = jwtUtils.generateJwtToken(authentication, user.getLogin(), user.getPassword());
		if (jwtToken == null) {
			throw new NullPointerException("JwtToken is null");
		}
		return Pair.of(user, jwtToken);
	}

	public User updateUser(String jwtToken, String description, String phoneNumber, String urlTag) {
		String login = jwtUtils.getUsernameFromJwtToken(jwtToken);
		User user = userInfoRepo.getByLogin(login);
		user.setDescription(description);
		user.setPhoneNumber(phoneNumber);
		user.setUrlTag(urlTag);
		userInfoRepo.update(user);
		return userInfoRepo.getByLogin(login);
	}

	public String login(String login, String password) {
		try {
			User user = userInfoRepo.getByLogin(login);
			if (!user.isActivated()) {
				throw new NotAuthorizedException("Not authorized: Account is not activated");
			}
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							login,
							password
					)
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			log.debug("User authenticated: {}", user.toString());
			return jwtUtils.generateJwtToken(authentication, user.getLogin(), user.getPassword());
		} catch (UsernameNotFoundException | BadCredentialsException ex) {
			throw new NotAuthorizedException("Not authorized: Bad credentials");
		}
	}

	public void logout(Cookie cookie) {
		jwtUtils.invalidateJwtToken(cookie);
	}
}
