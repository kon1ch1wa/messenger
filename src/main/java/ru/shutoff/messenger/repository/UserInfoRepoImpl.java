package ru.shutoff.messenger.repository;

import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.exception.DuplicateUserException;
import ru.shutoff.messenger.exception.InvalidTokenException;
import ru.shutoff.messenger.model.User;

@RequiredArgsConstructor
@Component
public class UserInfoRepoImpl implements UserInfoRepo {
	private static final String SQL_SAVE = "insert into users(id, email, login, name, password, is_activated, token) values (?, ?, ?, ?, ?, ?, ?)";
	private static final String SQL_UPDATE = "update users set password=?, is_activated=?, description=coalesce(?, description), phone_number=coalesce(?, phone_number), url_tag=coalesce(?, url_tag), token=? where id=?";
	private static final String SQL_GET_USER_BY_ID = "select * from users where id=?";
	private static final String SQL_GET_USER_BY_EMAIL = "select * from users where email=?";
	private static final String SQL_GET_USER_BY_LOGIN = "select * from users where login=?";
	private static final String SQL_GET_USER_BY_TAG = "select * from users where url_tag=?";
	private static final String SQL_GET_USER_BY_TOKEN = "select * from users where token=?";

	private final JdbcTemplate jdbcTemplate;

	private final @NonNull RowMapper<User> userMapper = (rs, rowNum) -> new User(
			rs.getObject("id", UUID.class),
			rs.getString("email"),
			rs.getString("login"),
			rs.getString("name"),
			rs.getString("password"),
			rs.getBoolean("is_activated"),
			rs.getString("description"),
			rs.getString("phone_number"),
			rs.getString("url_tag"),
			rs.getString("token")
	);

	@Override
	public void save(User user) {
		try {
			jdbcTemplate.update(SQL_SAVE, user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getPassword(), user.isActivated(), user.getToken());
		} catch (DataAccessException ex) {
			String err = String.format("User with this email or login already exists: %s", ex.getMessage());
			throw new DuplicateUserException(err);
		}
	}

	@Override
	public void update(User user) {
		try {
			jdbcTemplate.update(SQL_UPDATE, user.getPassword(), user.isActivated(), user.getDescription(), user.getPhoneNumber(), user.getUrlTag(), user.getToken(), user.getId());
		} catch (DuplicateKeyException ex) {
			throw new DuplicateUserException("Some data that should be unique is duplicated, please check.");
		} catch (DataAccessException ex) {
			throw new UsernameNotFoundException("No such user");
		}
	}

	@Override
	public User getById(String userId) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_USER_BY_ID, userMapper, userId);
		} catch (DataAccessException ex) {
			throw new UsernameNotFoundException("No user with this id");
		}
	}

	@Override
	public User getByEmail(String email) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_USER_BY_EMAIL, userMapper, email);
		} catch (DataAccessException ex) {
			throw new UsernameNotFoundException("No user with this email");
		}
	}

	@Override
	public User getByLogin(String login) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_USER_BY_LOGIN, userMapper, login);
		} catch (DataAccessException ex) {
			throw new UsernameNotFoundException("No user with this login");
		}
	}

	@Override
	public User getByUrlTag(String urlTag) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_USER_BY_TAG, userMapper, urlTag);
		} catch (DataAccessException ex) {
			throw new UsernameNotFoundException("No user with this url tag");
		}
	}

	@Override
	public User getByUnqiueToken(String token) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_USER_BY_TOKEN, userMapper, token);
		} catch (DataAccessException ex) {
			throw new InvalidTokenException(ex.getMessage());
		}
	}
}
