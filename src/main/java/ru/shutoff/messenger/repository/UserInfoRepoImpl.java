package ru.shutoff.messenger.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.shutoff.messenger.exception.DuplicateUserException;
import ru.shutoff.messenger.exception.InvalidTokenException;
import ru.shutoff.messenger.model.User;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class UserInfoRepoImpl implements UserInfoRepo {
	private static final String SQL_SAVE = "insert into users_data(id, email, login, password, is_activated) values (?, ?, ?, ?, ?)";
	private static final String SQL_ADD_TOKEN = "update users_data set token=? where id=?";
	private static final String SQL_GET_BY_TOKEN = "select id, email, login, password, is_activated, token from users_data where token=?";
	private static final String SQL_DELETE_TOKEN = "update users_data set token=NULL where token=?";
	private final JdbcTemplate jdbcTemplate;


	private final RowMapper<User> primaryUserMapper = (rs, rowNum) -> new User(
			rs.getObject("id", UUID.class),
			rs.getString("email"),
			rs.getString("login"),
			rs.getString("password"),
			rs.getBoolean("is_activated"),
			null, null, null,
			rs.getString("token")
	);
	@Override
	public void save(User user) {
		try {
			jdbcTemplate.update(SQL_SAVE, user.getId(), user.getEmail(), user.getLogin(), user.getPassword(), user.isActivated());
		} catch (DuplicateKeyException ex) {
			throw new DuplicateUserException("This email or login are already taken");
		}
	}

	@Override
	public void addToken(UUID userId, String token) {
		try {
			jdbcTemplate.update(SQL_ADD_TOKEN, token, userId);
		} catch (DuplicateKeyException ex) {
			throw new DuplicateUserException("This email or login are already taken");
		}
	}

	@Override
	public User getByToken(String token) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_BY_TOKEN, primaryUserMapper, token);
		} catch (DataAccessException ex) {
			throw new InvalidTokenException(ex.getMessage());
		}
	}

	@Override
	public void deleteToken(String token) {
		try {
			jdbcTemplate.update(SQL_DELETE_TOKEN, token);
		} catch (DataAccessException ex) {
			throw new InvalidTokenException(ex.getMessage());
		}
	}
}
