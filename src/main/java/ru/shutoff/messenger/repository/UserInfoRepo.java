package ru.shutoff.messenger.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import ru.shutoff.messenger.model.User;

@Repository
public interface UserInfoRepo {
	void save(User user);
	User getPrimary(String token);
	void update(User user);
	String getLoginByEmail(String email);
	String getEmailByLogin(String login);
	User getById(UUID userId);
	User getByEmail(String email);
	User getByLogin(String login);
}
