package ru.shutoff.messenger.repository;

import org.springframework.stereotype.Repository;
import ru.shutoff.messenger.model.User;

@Repository
public interface UserInfoRepo {
	void save(User user);
}
