package ru.shutoff.messenger.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.shutoff.messenger.exception.DuplicateUserException;
import ru.shutoff.messenger.model.User;

@RequiredArgsConstructor
@Component
public class UserInfoRepoImpl implements UserInfoRepo {
	private static final String SQL_SAVE = "insert into users_data(id, email, login, password) values (?, ?, ?, ?)";
	private final JdbcTemplate jdbcTemplate;
	@Override
	public void save(User user) {
		try {
			jdbcTemplate.update(SQL_SAVE, user.getId(), user.getEmail(), user.getLogin(), user.getPassword());
		} catch (DuplicateKeyException ex) {
			throw new DuplicateUserException("This email or login are already taken");
		}
	}
}
