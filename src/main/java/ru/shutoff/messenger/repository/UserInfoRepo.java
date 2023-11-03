package ru.shutoff.messenger.repository;

import org.springframework.stereotype.Repository;
import ru.shutoff.messenger.model.User;

import java.util.UUID;

@Repository
public interface UserInfoRepo {
	void save(User user);
	User getPrimary(String token);
	void update(User user);
	String getLoginByEmail(String email);
	String getEmailByLogin(String login);
	User getById(String userId);
	User getByEmail(String email);
	User getByLogin(String login);
	User getByUrlTag(String urlTag);
}
