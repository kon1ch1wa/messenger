package ru.shutoff.messenger.domain.user_mgmt.repository;

import org.springframework.stereotype.Repository;

import ru.shutoff.messenger.domain.user_mgmt.model.User;

@Repository
public interface UserInfoRepo {
	void save(User user);
	void update(User user);
	User getById(String userId);
	User getByEmail(String email);
	User getByLogin(String login);
	User getByUrlTag(String urlTag);
	User getByUnqiueToken(String token);
}
