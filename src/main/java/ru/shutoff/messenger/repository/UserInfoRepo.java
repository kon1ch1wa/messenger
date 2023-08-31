package ru.shutoff.messenger.repository;

import org.springframework.stereotype.Repository;
import ru.shutoff.messenger.model.User;

import java.util.UUID;

@Repository
public interface UserInfoRepo {
	void save(User user);
	void addToken(UUID userId, String token);
	User getByToken(String token);
	void deleteToken(String token);
}
