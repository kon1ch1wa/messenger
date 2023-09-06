package ru.shutoff.messenger.repository;

import org.springframework.stereotype.Repository;
import ru.shutoff.messenger.model.User;

import java.util.UUID;

@Repository
public interface UserInfoRepo {
	void save(User user);
	User getPrimary(String token);
	void update(User user);
	void updateValueById(String type, String value, UUID userId);
	void updateValueByEmail(String type, String value, String email);
	void updateValueByLogin(String type, String value, String login);
	String getLoginByEmail(String email);
	String getEmailByLogin(String login);
	User getById(UUID userId);
	User getByEmail(String email);
	User getByLogin(String login);
}
