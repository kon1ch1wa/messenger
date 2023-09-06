package ru.shutoff.messenger.repository;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.shutoff.messenger.exception.DuplicateUserException;
import ru.shutoff.messenger.exception.InvalidTokenException;
import ru.shutoff.messenger.model.User;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class UserInfoRepoImpl implements UserInfoRepo {
	private static final String SQL_SAVE = "insert into users_data(id, email, login, password, is_activated, token) values (?, ?, ?, ?, ?, ?)";
	private static final String SQL_GET_USER_BY_TOKEN = "select id, email, login, password, is_activated, description, phone_number, url_tag, token from users_data where token=?";
	private static final String SQL_UPDATE = "update users_data set email=?, login=?, password=?, is_activated=?, description=?, phone_number=?, url_tag=?, token=? where id=?";
	private static final String SQL_GET_USER_BY_ID = "select id, email, login, password, is_activated, description, phone_number, url_tag, token from users_data where id=?";
	private static final String SQL_GET_USER_BY_EMAIL = "select id, email, login, password, is_activated, description, phone_number, url_tag, token from users_data where email=?";
	private static final String SQL_GET_USER_BY_LOGIN = "select id, email, login, password, is_activated, description, phone_number, url_tag, token from users_data where login=?";
	private static final String SQL_GET_LOGIN_BY_EMAIL = "select login from users_data where email=?";
	private static final String SQL_GET_EMAIL_BY_LOGIN = "select email from users_data where login=?";

	private final JdbcTemplate jdbcTemplate;

	private final RowMapper<User> userMapper = (rs, rowNum) -> new User(
			rs.getObject("id", UUID.class),
			rs.getString("email"),
			rs.getString("login"),
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
			jdbcTemplate.update(SQL_SAVE, user.getId(), user.getEmail(), user.getLogin(), user.getPassword(), user.isActivated(), user.getToken());
		} catch (DataAccessException ex) {
			throw new DuplicateUserException("User with this email or login already exists");
		}
	}

	@Override
	public User getPrimary(String token) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_USER_BY_TOKEN, userMapper, token);
		} catch (DataAccessException ex) {
			throw new InvalidTokenException(ex.getMessage());
		}
	}

	@Override
	public void update(User user) {
		try {
			jdbcTemplate.update(SQL_UPDATE, user.getEmail(), user.getLogin(), user.getPassword(), user.isActivated(), user.getDescription(), user.getPhoneNumber(), user.getUrlTag(), user.getToken(), user.getId());
		} catch (DataAccessException ex) {
			throw new DuplicateUserException("Some data that should be unique is duplicated, please check.");
		}
	}

	@Override
	public void updateValueById(String type, String value, UUID userId) {
		try {
			jdbcTemplate.update(String.format("update users_data set %s=? where id=?", type), value, userId);
		} catch (DataAccessException ex) {
			throw new UsernameNotFoundException("No user with this id");
		}
	}

	@Override
	public void updateValueByEmail(String type, String value, String email) {
		try {
			jdbcTemplate.update(String.format("update users_data set %s=? where email=?", type), value, email);
		} catch (DataAccessException ex) {
			throw new UsernameNotFoundException("No user with this email");
		}
	}

	@Override
	public void updateValueByLogin(String type, String value, String login) {
		try {
			jdbcTemplate.update(String.format("update users_data set %s=? where login=?", type), value, login);
		} catch (DataAccessException ex) {
			throw new UsernameNotFoundException("No user with this login");
		}
	}

	@Override
	public User getById(UUID userId) {
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
	public String getLoginByEmail(String email) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_LOGIN_BY_EMAIL, String.class, email);
		} catch (DataAccessException ex) {
			throw new UsernameNotFoundException("No user with this email");
		}
	}

	@Override
	public String getEmailByLogin(String login) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_EMAIL_BY_LOGIN, String.class, login);
		} catch (DataAccessException ex) {
			throw new UsernameNotFoundException("No user with this login");
		}
	}
}
